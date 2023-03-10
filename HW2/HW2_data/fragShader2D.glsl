#version 430

in vec4 c;
out vec4 color;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
layout (binding=0) uniform sampler2D s;

void main(void)
{
	color = c;
}
