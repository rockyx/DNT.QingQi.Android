/*
 * RGlobal.h
 *
 *  Created on: 2014-2-27
 *      Author: Rocky Tsui
 */
#ifdef _MSC_VER
#pragma once
#endif

#ifndef __DNT_RGLOBAL_H__
#define __DNT_RGLOBAL_H__

#include <cstdint>
#include <cstdlib>
#include <vector>
#include <string>
#include <sstream>
#include <iomanip>
#include <iostream>
#include <termios.h>
#include <RSystemDetection.h>

#ifdef DNTDIAG_BUILD
#ifdef _MSC_VER
#define R_DIAG_EXPORT __declspec(dllexport)
#else
#define R_DIAG_EXPORT
#endif
#else
#ifdef _MSC_VER
#define R_DIAG_EXPORT __declspec(dllimport)
#else
#define R_DIAG_EXPORT
#endif
#endif

#ifdef DNTECU_BUILD
#ifdef _MSC_VER
#define R_ECU_EXPORT __declspec(dllexport)
#else
#define R_ECU_EXPORT
#endif
#else
#ifdef _MSC_VER
#define R_ECU_EXPORT __declspec(dllimport)
#else
#define R_ECU_EXPORT
#endif
#endif

#undef MAX
#define MAX(a, b) (((a) > (b)) ? (a) : (b))

#undef MIN
#define MIN(a, b) (((a) < (b)) ? (a) : (b))

#ifdef __cplusplus
#define R_DIAG_BEGIN_DECLS extern "C" {
#define R_DIAG_END_DECLS }
#else
#define R_DIAG_BEGIN_DECLS
#define R_DIAG_END_DECLS
#endif

#ifndef FALSE
#define FALSE 0
#endif

#ifndef TRUE
#define TRUE 1
#endif

typedef int RBoolean;
typedef void * RHandle;


#endif /* __DNT_RGLOBAL_H__ */
