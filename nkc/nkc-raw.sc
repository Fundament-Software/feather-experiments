
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

    type+ nk_rect
        inline... __typecall
        case (cls, x : f32, y : f32, w : f32, h : f32)
            module.extern.nk_rect x y w h
        case (cls, x : i32, y : i32, w : i32, h : i32)
            module.extern.nk_recti x y w h
        case (cls, pos : nk_vec2, size : nk_vec2)
            module.extern.nk_recta pos size
        case (cls, xywh : (@ f32))
            module.extern.nk_rectv xywh

    nk_image_show := module.extern.nk_image
    nk_text := module.extern.nk_text

    nk_rectf := module.extern.nk_rect

    locals;
