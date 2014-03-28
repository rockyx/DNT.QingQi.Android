#include <fcntl.h>
#include <dirent.h>
#include <stdio.h>
#include <errno.h>
#include <unistd.h>
#include <sys/time.h>
#include <sys/ioctl.h>
#include <sys/select.h>
#include <sys/poll.h>
#include <android/log.h>
#include <jni.h>
#include "RGlobal.h"

#define R_SERIAL_HANDSHAKE_NONE 0
#define R_SERIAL_HANDSHAKE_XONXOFF 1
#define R_SERIAL_HANDSHAKE_REQUESTTOSEND 2
#define R_SERIAL_HANDSHAKE_REQUESTTOSENDXONXOFF 3

#define R_SERIAL_PARITY_NONE 0
#define R_SERIAL_PARITY_ODD 1
#define R_SERIAL_PARITY_EVEN 2
#define R_SERIAL_PARITY_MARK 3
#define R_SERIAL_PARITY_SPACE 4

#define R_SERIAL_STOPBITS_NONE 0
#define R_SERIAL_STOPBITS_ONE 1
#define R_SERIAL_STOPBITS_TWO 2
#define R_SERIAL_STOPBITS_ONE5 3

#define R_SERIAL_NONE_SIGNAL 0
#define R_SERIAL_CD 1 // Carrier detect
#define R_SERIAL_CTS 2 // Clear to send
#define R_SERIAL_DSR 4 // Data set ready
#define R_SERIAL_DTR 8 // Data terminal ready
#define R_SERIAL_RTS 16 // Request to send
using namespace std;

////////////// JNI Methods /////////////////////////////////////////////////////
R_DIAG_BEGIN_DECLS

JNIEXPORT jstring JNICALL
Java_dnt_diag_io_NativeMethods_getLastError(JNIEnv *env, jclass cls) {
	jstring str = env->NewStringUTF(strerror(errno));
	return str;
}

JNIEXPORT jint JNICALL
Java_dnt_diag_io_NativeMethods_openSerial(JNIEnv *env, jclass cls,
		jstring portName) {
	int fd;
	const char *devfile = env->GetStringUTFChars(portName, NULL);
	fd = open(devfile, O_RDWR | O_NOCTTY | O_NONBLOCK);

#if 0
	stringstream ss;
	ss << "Open dev file name: " << devfile << " fd = " << fd;
	__android_log_write(ANDROID_LOG_VERBOSE, "SerialPort", ss.str().c_str());
#endif

	env->ReleaseStringUTFChars(portName, devfile);
	return fd;
}

JNIEXPORT jint JNICALL
Java_dnt_diag_io_NativeMethods_closeSerial(JNIEnv *env, jclass cls,
		jint unixFD) {
	// Linus writes: do not retry close after EINTR
	return close(unixFD);
}

JNIEXPORT jint JNICALL
Java_dnt_diag_io_NativeMethods_readSerial(JNIEnv *env, jclass cls, jint fd,
		jbyteArray array, jint offset, jint count) {
	jbyte buff[1024];
	jint n;

	n = read(fd, buff, count);

	env->SetByteArrayRegion(array, offset, n, buff);

#if 0
	if (n > 0) {
		stringstream ss;
		for (int i = 0; i < n; i++)
			ss << " " << setfill('0') << setw(2) << hex << static_cast<uint32_t>(buff[i] & 0xFF);
		__android_log_write(ANDROID_LOG_VERBOSE, "SerialPort Read", ss.str().c_str());
	}
#endif

	return n;
}

JNIEXPORT jboolean JNICALL
Java_dnt_diag_io_NativeMethods_pollSerial(JNIEnv *env, jclass cls, jint fd,
		jint timeout) {
	struct pollfd pinfo;
	jfieldID errorID = env->GetStaticFieldID(cls, "error", "I");

	env->SetStaticIntField(cls, errorID, 0);

	pinfo.fd = fd;
	pinfo.events = POLLIN;
	pinfo.revents = 0;

	while (poll(&pinfo, 1, timeout) == -1 && errno == EINTR) {
		// EINTR is an OK condition, we should not throw in the upper layer an IOException
		if (errno != EINTR) {
			env->SetStaticIntField(cls, errorID, -1);
			return JNI_FALSE;
		}
	}

	return (pinfo.revents & POLLIN) != 0 ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL
Java_dnt_diag_io_NativeMethods_writeSerial(JNIEnv *env, jclass cls, jint fd,
		jbyteArray array, jint offset, jint count, jint timeout) {
#if defined(TOMIC_DEVICE_V1)
	jbyte * buffer = env->GetByteArrayElements(array, NULL);

	jint ret = write(fd, buffer + offset, count) == count ? 0 : -1;

	env->ReleaseByteArrayElements(array, buffer, 0);

	return ret;
#else
	struct pollfd pinfo;
	uint32_t n;

	pinfo.fd = fd;
	pinfo.events = POLLOUT;
	pinfo.revents = POLLOUT;

	n = count;

	jbyte * buffer = env->GetByteArrayElements(array, NULL);

	while (n > 0) {
		ssize_t t;

		if (timeout != 0) {
			int c;

			while ((c == poll(&pinfo, 1, timeout)) == -1 && errno == EINTR)
				;

			if (c == -1) {
				env->ReleaseByteArrayElements(array, buffer, 0);
				return -1;
			}
		}

		do {
			t = write(fd, buffer + offset, n);
		} while (t == -1 && errno == EINTR);

		if (t < 0) {
			env->ReleaseByteArrayElements(array, buffer, 0);
			return -1;
		}

		offset += t;
		n -= t;
	}

	env->ReleaseByteArrayElements(array, buffer, 0);
	return 0;
#endif
}

JNIEXPORT jint JNICALL
Java_dnt_diag_io_NativeMethods_discardBuffer(JNIEnv *env, jclass cls, jint fd,
		jboolean input) {
	return tcflush(fd, input ? TCIFLUSH : TCOFLUSH);
}

JNIEXPORT jint JNICALL
Java_dnt_diag_io_NativeMethods_getBytesInBuffer(JNIEnv *env, jclass cls,
		jint fd, jboolean input) {
	int32_t retval;

	if (ioctl(fd, input ? FIONREAD : TIOCOUTQ, &retval) == -1) {
		return -1;
	}

	return retval;
}

static int setupBaudRate(int baudRate) {
	switch (baudRate) {
	// Some values are not defined on OSX and *BSD
#if defined(B921600)
	case 921600:
		return B921600;
#endif
#if defined(B460800)
	case 460800:
		return B460800;
#endif
	case 230400:
		return B230400;
	case 115200:
		return B115200;
	case 57600:
		return B57600;
	case 38400:
		return B38400;
	case 19200:
		return B19200;
	case 9600:
		return B9600;
	case 4800:
		return B4800;
	case 2400:
		return B2400;
	case 1800:
		return B1800;
	case 1200:
		return B1200;
	case 600:
		return B600;
	case 300:
		return B300;
	case 200:
		return B200;
	case 150:
		return B150;
	case 134:
		return B134;
	case 110:
		return B110;
	case 75:
		return B75;
	case 50:
	case 0:
	default:
		return -1;
	}
}

JNIEXPORT jboolean JNICALL
Java_dnt_diag_io_NativeMethods_isBaudRateLegal(JNIEnv *env, jclass,
		int baudRate) {
	return setupBaudRate(baudRate) != -1 ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_dnt_diag_io_NativeMethods_setAttributes(JNIEnv *env, jclass cls, jint fd,
		jint baudRate, jint parity, jint dataBits, jint stopBits,
		jint handshake) {
	struct termios newtio;

	if (tcgetattr(fd, &newtio) == -1)
		return JNI_FALSE;

#if 0
	stringstream ss;
	ss << "Setting attributes, baudrate = " << baudRate << " parity = "
			<< parity << " databits = " << dataBits << " stopbits = "
			<< stopBits << " handshake = " << handshake;
	__android_log_write(ANDROID_LOG_VERBOSE, "SerialPort", ss.str().c_str());
#endif

	newtio.c_cflag |= (CLOCAL | CREAD);
	newtio.c_lflag &= ~(ICANON | ECHO | ECHOE | ECHOK | ECHONL | ISIG | IEXTEN);
	newtio.c_oflag &= ~(OPOST);
	newtio.c_iflag = IGNBRK;

	// setup baudrate
	baudRate = setupBaudRate(baudRate);

	// char length
	newtio.c_cflag &= ~CSIZE;
	switch (dataBits) {
	case 5:
		newtio.c_cflag |= CS5;
		break;
	case 6:
		newtio.c_cflag |= CS6;
		break;
	case 7:
		newtio.c_cflag |= CS7;
		break;
	case 8:
	default:
		newtio.c_cflag |= CS8;
		break;
	}

	// stopbits
	switch (stopBits) {
	case R_SERIAL_STOPBITS_NONE:
		// Unhandled
		break;
	case R_SERIAL_STOPBITS_ONE: // One
		// do nothing, the default is one stop bit
		newtio.c_cflag &= ~CSTOPB;
		break;
	case R_SERIAL_STOPBITS_TWO: // Two
		newtio.c_cflag |= CSTOPB;
		break;
	case R_SERIAL_STOPBITS_ONE5: // OnePointFive
		// unhandled
		break;
	}

	// parity
	newtio.c_iflag &= ~(INPCK | ISTRIP);

	switch (parity) {
	case R_SERIAL_PARITY_NONE: // None
		newtio.c_cflag &= ~(PARENB | PARODD);
		break;
	case R_SERIAL_PARITY_ODD: // Odd
		newtio.c_cflag |= PARENB | PARODD;
		break;
	case R_SERIAL_PARITY_EVEN: // Even
		newtio.c_cflag &= ~(PARODD);
		newtio.c_cflag |= (PARENB);
		break;
	case R_SERIAL_PARITY_MARK:
		// Unhandled
		break;
	case R_SERIAL_PARITY_SPACE:
		// unhandled
		break;
	}

	newtio.c_iflag &= ~(IXOFF | IXON);
#ifdef CRTSCTS
	newtio.c_cflag &= ~CRTSCTS;
#endif /* def CRTSCTS */
	switch (handshake) {
	case R_SERIAL_HANDSHAKE_NONE: // None
		// do nothing
		break;
	case R_SERIAL_HANDSHAKE_REQUESTTOSEND: // RequestToSend (RTS)
#ifdef CRTSCTS
		newtio.c_cflag |= CRTSCTS;
#endif
		break;
	case R_SERIAL_HANDSHAKE_REQUESTTOSENDXONXOFF: // RequestToSendXOnXOff (RTS + XON/XOFF)
#ifdef CRTSCTS
		newtio.c_cflag |= CRTSCTS;
#endif
		// fall through
	case R_SERIAL_HANDSHAKE_XONXOFF: // XOnXOff
		newtio.c_iflag |= IXOFF | IXON;
		break;
	}

	if (cfsetospeed(&newtio, baudRate) < 0 || cfsetispeed(&newtio, baudRate) < 0
			|| tcsetattr(fd, TCSANOW, &newtio) < 0)
		return JNI_FALSE;

	return JNI_TRUE;

}

static int32_t getSignalCode(int32_t signal) {
	switch (signal) {
	case R_SERIAL_CD:
		return TIOCM_CAR;
	case R_SERIAL_CTS:
		return TIOCM_CTS;
	case R_SERIAL_DSR:
		return TIOCM_DSR;
	case R_SERIAL_DTR:
		return TIOCM_DTR;
	case R_SERIAL_RTS:
		return TIOCM_RTS;
	default:
		return 0;
	}

	// Not reached
	return 0;
}

static int32_t getJavaSignalCodes(int signals) {
	int32_t retval = R_SERIAL_NONE_SIGNAL;

	if ((signals & TIOCM_CAR) != 0)
		retval |= R_SERIAL_CD;
	if ((signals & TIOCM_CTS) != 0)
		retval |= R_SERIAL_CTS;
	if ((signals & TIOCM_DSR) != 0)
		retval |= R_SERIAL_DSR;
	if ((signals & TIOCM_DTR) != 0)
		retval |= R_SERIAL_DTR;
	if ((signals & TIOCM_RTS) != 0)
		retval |= R_SERIAL_RTS;

	return retval;
}

JNIEXPORT jint JNICALL
Java_dnt_diag_io_NativeMethods_getSignals(JNIEnv *env, jclass cls, jint fd) {
	int signals;

	jfieldID errorID = env->GetStaticFieldID(cls, "error", "I");

	env->SetStaticIntField(cls, errorID, 0);

	if (ioctl(fd, TIOCMGET, &signals) == -1) {
		env->SetStaticIntField(cls, errorID, -1);
		return R_SERIAL_NONE_SIGNAL;
	}

	return getJavaSignalCodes(signals);
}

JNIEXPORT jint JNICALL
Java_dnt_diag_io_NativeMethods_setSignal(JNIEnv *env, jclass cls, jint fd,
		jint signal, jboolean value) {

	if (signal == R_SERIAL_DTR) {
		if (value) {
#if 0
			__android_log_write(ANDROID_LOG_VERBOSE, "SerialPort",
					"pulling down");
#endif
			system("/system/bin/pulldown");
		} else {
#if 0
			__android_log_write(ANDROID_LOG_VERBOSE, "SerialPort",
					"pulling up");
#endif
			system("/system/bin/pullup");
		}
	}

	int signals, expected, activated;

	expected = getSignalCode(signal);
	if (ioctl(fd, TIOCMGET, &signals) == -1)
		return -1;

	activated = (signals & expected) != 0;
	if (activated == value) // Already set
		return 1;

	if (value)
		signals |= expected;
	else
		signals &= ~expected;

	if (ioctl(fd, TIOCMSET, &signals) == -1)
		return -1;

	return 1;
}

JNIEXPORT jint JNICALL
Java_dnt_diag_io_NativeMethods_breakprop(JNIEnv *env, jclass cls, jint fd) {
	return tcsendbreak(fd, 0);
}

R_DIAG_END_DECLS
