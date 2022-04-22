
# using import .postprocess-library-bindings
using import spicetools
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

    # nk_rect.__typecall =
    #     spice "__typecall" (cls args...)
    #         spice-match args...
    #         case (x : f32, y : f32, w : f32, h : f32)
    #             `(module.extern.nk_rect x y w h)
    #         case (x : i32, y : i32, w : i32, h : i32)
    #             `(module.extern.nk_recti x y w h)
    #         case (pos : nk_vec2, size : nk_vec2)
    #             `(module.extern.nk_recta pos size)
    #         # case (xywh : (@ f32))
    #         #     module.extern.nk_rectv xywh
    #         default
    #             error "attempted to construct a rectangle with unexpected arguments"

    nk_rectf := module.extern.nk_rect

    locals;
