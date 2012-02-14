#pp header

#pp order 500
#pp entry glowblur
void glowblur(inout vec4 res);

#pp main

uniform sampler2D glowtex;
uniform float glowtex_xs, glowtex_ys;

void glowblur(inout vec4 res)
{
    vec2 c = gl_TexCoord[0].st;
    vec4 acc = texture2D(glowtex, c);
    acc = max(acc, texture2D(glowtex, c + vec2(glowtex_xs, 0.0)));
    acc = max(acc, texture2D(glowtex, c - vec2(glowtex_xs, 0.0)));
    acc = max(acc, texture2D(glowtex, c + vec2(0.0, glowtex_ys)));
    acc = max(acc, texture2D(glowtex, c - vec2(0.0, glowtex_ys)));
    acc = max(acc, texture2D(glowtex, c + vec2(glowtex_xs * 2.0, 0.0)) * 0.75);
    acc = max(acc, texture2D(glowtex, c - vec2(glowtex_xs * 2.0, 0.0)) * 0.75);
    acc = max(acc, texture2D(glowtex, c + vec2(0.0, glowtex_ys * 2.0)) * 0.75);
    acc = max(acc, texture2D(glowtex, c - vec2(0.0, glowtex_ys * 2.0)) * 0.75);
    res *= acc;
}
