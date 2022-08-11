#version 120
attribute vec4 vPosition;
void main() {
    // vPosition是顶点，由应用程序传入。
    gl_Position = vPosition;
}
