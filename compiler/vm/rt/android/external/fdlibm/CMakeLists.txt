project(android-external-fdlibm)

# This is necessary to guarantee that the FDLIBM functions are in
# "IEEE spirit   i.e. to guarantee that the IEEE 754 core functions
# are used.
add_compile_options(-D_IEEE_LIBM)

# Disable GCC optimizations that interact badly with this crufty
# library (see their own admission in 'readme'). Without this, we
# fail StrictMath tests on x86.
add_compile_options(-fno-strict-aliasing)

# fdlibm expects __LITTLE_ENDIAN to be defined for little endian targets but
# clang defines __LITTLE_ENDIAN__.
if(DARWIN)
  add_definitions(-D__LITTLE_ENDIAN=__LITTLE_ENDIAN__)
endif()

# Disable warnings. We need a specific version of fdlibm and can't fix this upstream.
add_compile_options(-Wno-sign-compare)
add_compile_options(-Wno-dangling-else)
add_compile_options(-Wno-unknown-pragmas)
add_compile_options(-Wno-dangling-else)
add_compile_options(-Wno-logical-op-parentheses)
add_compile_options(-Wno-sometimes-uninitialized)

set(SRC
  k_standard.c  
  k_rem_pio2.c  
  k_cos.c  
  k_sin.c  
  k_tan.c  
  e_acos.c  
  e_acosh.c  
  e_asin.c  
  e_atan2.c  
  e_atanh.c  
  e_cosh.c  
  e_exp.c  
  e_fmod.c  
  e_gamma.c  
  e_gamma_r.c  
  e_hypot.c  
  e_j0.c  
  e_j1.c  
  e_jn.c  
  e_lgamma.c  
  e_lgamma_r.c  
  e_log.c  
  e_log10.c  
  e_pow.c  
  e_rem_pio2.c  
  e_remainder.c  
  e_scalb.c  
  e_sinh.c  
  e_sqrt.c  
  w_acos.c  
  w_acosh.c  
  w_asin.c  
  w_atan2.c  
  w_atanh.c  
  w_cosh.c  
  w_exp.c  
  w_fmod.c  
  w_gamma.c  
  w_gamma_r.c  
  w_hypot.c  
  w_j0.c  
  w_j1.c  
  w_jn.c  
  w_lgamma.c  
  w_lgamma_r.c  
  w_log.c  
  w_log10.c  
  w_pow.c  
  w_remainder.c  
  w_scalb.c  
  w_sinh.c  
  w_sqrt.c  
  s_asinh.c  
  s_atan.c  
  s_cbrt.c  
  s_ceil.c  
  s_copysign.c  
  s_cos.c  
  s_erf.c  
  s_expm1.c  
  s_fabs.c  
  s_finite.c  
  s_floor.c  
  s_frexp.c  
  s_ilogb.c  
  s_isnan.c  
  s_ldexp.c  
  s_lib_version.c  
  s_log1p.c  
  s_logb.c  
  s_matherr.c  
  s_modf.c  
  s_nextafter.c  
  s_rint.c  
  s_scalbn.c  
  s_signgam.c  
  s_significand.c  
  s_sin.c  
  s_tan.c  
  s_tanh.c  
)

add_library(android-external-fdlibm OBJECT ${SRC})
set(EXTERNAL_FDLIBM_OBJECTS $<TARGET_OBJECTS:android-external-fdlibm> PARENT_SCOPE)