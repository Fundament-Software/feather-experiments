
using import .nkc


ui main-ui (state)
    window "test window" (rectf 50 50 220 220) 0
        text "Some sample text." TEXT_LEFT

fn main()
    local state = 0.5
    start state main-ui "Test code" 640 480 WIN_NORMAL

main;
