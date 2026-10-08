#version 330 core

// Draws every kind of GUI shape as a signed distance field, so edges are anti-aliased at any size.
//
// All lengths are in framebuffer pixels. shapeA holds (half width, half height, extra, mode) where extra is the border
// width for boxes, the blur sigma for shadows and the thickness for lines. shapeB holds the corner radii as
// (top left, top right, bottom right, bottom left).
//
// Modes: 0 box, 1 shadow, 2 glass (box over the blurred backdrop), 3 line (half width and height are the half vector
// of the segment)

uniform sampler2D u_Backdrop;

in vec2 v_Local;
in vec2 v_ScreenUv;
in vec4 v_Fill;
flat in vec4 v_ShapeA;
flat in vec4 v_ShapeB;
flat in vec4 v_Border;

out vec4 color;

const float MODE_SHADOW = 1.0;
const float MODE_GLASS = 2.0;
const float MODE_LINE = 3.0;

// Abramowitz and Stegun 7.1.26
float erf(float x) {
    float s = sign(x);
    float a = abs(x);
    float t = 1.0 / (1.0 + 0.3275911 * a);
    float y = 1.0 - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t - 0.284496736) * t + 0.254829592) * t * exp(-a * a);
    return s * y;
}

// Distance to a box with a different radius on every corner, negative inside
float sdBox(vec2 p, vec2 halfSize, vec4 radii) {
    float radius = p.x < 0.0 ? (p.y < 0.0 ? radii.x : radii.w) : (p.y < 0.0 ? radii.y : radii.z);
    radius = min(radius, min(halfSize.x, halfSize.y));

    vec2 q = abs(p) - halfSize + radius;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius;
}

float sdSegment(vec2 p, vec2 a, vec2 b) {
    vec2 pa = p - a;
    vec2 ba = b - a;
    float h = clamp(dot(pa, ba) / max(dot(ba, ba), 0.000001), 0.0, 1.0);
    return length(pa - ba * h);
}

// How much of the pixel is inside the shape given its distance
float coverage(float d) {
    float aa = max(length(vec2(dFdx(d), dFdy(d))), 0.0001);
    return clamp(0.5 - d / aa, 0.0, 1.0);
}

void main() {
    float mode = v_ShapeA.w;
    vec2 halfSize = v_ShapeA.xy;
    float extra = v_ShapeA.z;

    if (mode == MODE_SHADOW) {
        float d = sdBox(v_Local, halfSize, v_ShapeB);
        float sigma = max(extra, 0.001);
        float alpha = 0.5 - 0.5 * erf(d / (sigma * 1.41421356));

        color = vec4(v_Fill.rgb, v_Fill.a * alpha);
    }
    else if (mode == MODE_LINE) {
        float d = sdSegment(v_Local, -halfSize, halfSize) - extra * 0.5;

        color = vec4(v_Fill.rgb, v_Fill.a * coverage(d));
    }
    else {
        float d = sdBox(v_Local, halfSize, v_ShapeB);

        vec3 rgb = v_Fill.rgb;
        float alpha = v_Fill.a;

        if (mode == MODE_GLASS) {
            // The fill is a tint on top of the blurred scene behind the shape
            rgb = mix(texture(u_Backdrop, v_ScreenUv).rgb, v_Fill.rgb, v_Fill.a);
            alpha = 1.0;
        }

        if (extra > 0.0) {
            float inner = coverage(d + extra);

            rgb = mix(v_Border.rgb, rgb, inner);
            alpha = mix(v_Border.a, alpha, inner);
        }

        color = vec4(rgb, alpha * coverage(d));
    }
}
