package com.example.vezdekodfinal.ui.filter

import com.daasuu.mp4compose.filter.GlFilter

private const val FADE_IN_FADE_OUT_FRAGMENT_SHARDER_SOURCE = """precision mediump float;
        uniform sampler2D sTexture;
        uniform float uCurrentTime;
        varying vec2 vTextureCoord;
        
        #define PI 3.14159265359
        
        #define PERIOD 2.0
        // -1.0 - black
        //  1.0 - white
        #define BLACKORWHITE -1.0
        #define INOUTKOEF 1.0
        
        float customMod(float x, float y) {
            return (x) - (y) * floor((x) / (y));
        }gi
        
        
        void main() {
            vec2 uv = vTextureCoord.xy;
            float timeColor = (1.0 - pow(abs(customMod(float(uCurrentTime) / PERIOD * 2.0, PERIOD) - 1.0), INOUTKOEF)) * BLACKORWHITE;
            vec4 color = texture2D(sTexture, uv);
            color.rgb = clamp(color.rgb + timeColor, vec3(0.0), vec3(1.0));
            gl_FragColor = color;
        }"""

class GlFadeInFadeOutFilter : GlFilter(DEFAULT_VERTEX_SHADER, FADE_IN_FADE_OUT_FRAGMENT_SHARDER_SOURCE)
