
# using import .postprocess-library-bindings
let extern-pattern = "^(nkc_|NKC_|nk_|NK_)"

let module =
    include
        """"#define NKC_IMPLEMENTATION
            #define NKC_USE_OPENGL 3
            #define NKCD NKC_GLFW
            #include "nuklear_cross.h"
        options "-ggdb"

load-library "libglfw.so"
load-library "libGLEW.so"

# vvv dump
do
    using module.extern filter extern-pattern
    using module.const filter extern-pattern
    using module.typedef filter extern-pattern
    using module.struct filter extern-pattern
    using module.enum filter extern-pattern
    using module.define filter extern-pattern
    using module.union filter extern-pattern
    let nkc = module.struct.nkc

    locals;
