#version 330 core

layout (location = 0) in vec2 pos;
layout (location = 1) in vec2 local;
layout (location = 2) in vec4 shapeA;
layout (location = 3) in vec4 shapeB;
layout (location = 4) in vec4 fill;
layout (location = 5) in vec4 border;

layout (std140) uniform MeshData {
    mat4 u_Proj;
    mat4 u_ModelView;
};

out vec2 v_Local;
out vec2 v_ScreenUv;
out vec4 v_Fill;
flat out vec4 v_ShapeA;
flat out vec4 v_ShapeB;
flat out vec4 v_Border;

void main() {
    gl_Position = u_Proj * u_ModelView * vec4(pos, 0.0, 1.0);

    v_Local = local;
    v_Fill = fill;
    v_ShapeA = shapeA;
    v_ShapeB = shapeB;
    v_Border = border;

    // Where this vertex is on the screen, used to look up the blurred backdrop
    v_ScreenUv = gl_Position.xy / gl_Position.w * 0.5 + 0.5;
}
