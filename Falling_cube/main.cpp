#include <iostream>
#include <vector>
#include <Windows.h>
#include "glut.h"
#include "time.h"

const double pi = 3.14159265358979323846;

double DeltaTime;

struct quaternion {
    double r, i, j, k;
};

struct vec {
    double x, y, z;
};

struct matrix {
    double m[3][3];

    matrix& operator=(matrix A) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                m[i][j] = A.m[i][j];
            }
        }
        return *this;
    }
};

double Dot(const vec& v1, const vec& v2) {
    return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
}

vec Cross(const vec& v1, const vec& v2) {
    return vec{ v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v2.x * v2.y - v1.y * v2.x };
}

void DrawBlock(double a, double b, double c) {
    glColor3f(1, 0, 0);
    glBegin(GL_QUADS);
    glVertex3f(a, b, c);
    glVertex3f(-a, b, c);
    glVertex3f(-a, -b, c);
    glVertex3f(a, -b, c);
    glEnd();
    glColor3f(1, 0, 0);
    glBegin(GL_QUADS);
    glVertex3f(a, b, -c);
    glVertex3f(a, -b, -c);
    glVertex3f(-a, -b, -c);
    glVertex3f(-a, b, -c);
    glEnd();
    glColor3f(0, 1, 0);
    glBegin(GL_QUADS);
    glVertex3f(-a, b, c);
    glVertex3f(-a, b, -c);
    glVertex3f(-a, -b, -c);
    glVertex3f(-a, -b, c);
    glEnd();
    glColor3f(0, 1, 0);
    glBegin(GL_QUADS);
    glVertex3f(a, b, c);
    glVertex3f(a, -b, c);
    glVertex3f(a, -b, -c);
    glVertex3f(a, b, -c);
    glEnd();
    glColor3f(0, 0, 1);
    glBegin(GL_QUADS);
    glVertex3f(-a, b, -c);
    glVertex3f(-a, b, c);
    glVertex3f(a, b, c);
    glVertex3f(a, b, -c);
    glEnd();
    glColor3f(0, 0, 1);
    glBegin(GL_QUADS);
    glVertex3f(-a, -b, -c);
    glVertex3f(a, -b, -c);
    glVertex3f(a, -b, c);
    glVertex3f(-a, -b, c);
    glEnd();
}

typedef struct RigidBody {
    matrix R;
    vec r, l, L;
    quaternion q;
} RigidBody;

RigidBody rb;

const matrix zero = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };

const double M_inv = 0.2;
matrix I_inv = zero;

const double width = 40, length = 40, height = 40;

matrix MatrMulMatr(matrix A, matrix B) {
    matrix mul;
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            mul.m[i][j] = 0;
            for (int k = 0; k < 3; k++) {
                mul.m[i][j] += A.m[i][k] * B.m[k][j];
            }
        }
    }
    return mul;
}

matrix Transpose(matrix A) {
    matrix transpose;
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            transpose.m[i][j] = A.m[j][i];
        }
    }
    return transpose;
}

vec MatrMulVec(matrix A, vec v) {
    vec res;
    res.x = A.m[0][0] * v.x + A.m[0][1] * v.y + A.m[0][2] * v.z;
    res.y = A.m[1][0] * v.x + A.m[1][1] * v.y + A.m[1][2] * v.z;
    res.z = A.m[2][0] * v.x + A.m[2][1] * v.y + A.m[2][2] * v.z;
    return res;
}

matrix Quat2Matr(const quaternion& q) {
    matrix m;
    m.m[0][0] = 1 - 2 * q.j * q.j - 2 * q.k * q.k;
    m.m[0][1] = 2 * q.i * q.j - 2 * q.r * q.k;
    m.m[0][2] = 2 * q.i * q.k + 2 * q.r * q.j;
    m.m[1][0] = 2 * q.i * q.j + 2 * q.r * q.k;
    m.m[1][1] = 1 - 2 * q.i * q.i - 2 * q.k * q.k;
    m.m[1][2] = 2 * q.j * q.k - 2 * q.r * q.i;
    m.m[2][0] = 2 * q.i * q.k - 2 * q.r * q.j;
    m.m[2][1] = 2 * q.j * q.k + 2 * q.r * q.i;
    m.m[2][2] = 1 - 2 * q.i * q.i - 2 * q.j * q.j;
    return m;
}

quaternion Normalize(const quaternion& q) {
    quaternion res = q;
    double len = sqrt(q.r * q.r + q.i * q.i + q.j * q.j + q.k * q.k);
    if (len != 0) {
        res.r = q.r / len;
        res.i = q.i / len;
        res.j = q.j / len;
        res.k = q.k / len;
    }
    return res;
}

quaternion QuatMulQuat(const quaternion& q1, const quaternion& q2) {
    quaternion res;
    res.r = q1.r * q2.r - q1.i * q2.i - q1.j * q2.j - q1.k * q2.k;

    res.i = q1.r * q2.i + q1.i * q2.r + q1.j * q2.k - q1.k * q2.j;
    res.j = q1.r * q2.j - q1.i * q2.k + q1.j * q2.r + q1.k * q2.i;
    res.k = q1.r * q2.k + q1.i * q2.j - q1.j * q2.i + q1.k * q2.r;
    return res;
}

RigidBody f(RigidBody rb) {
    RigidBody res;
    res.r.x = rb.l.x * M_inv;
    res.r.y = rb.l.y * M_inv;
    res.r.z = rb.l.z * M_inv;
    rb.R = Quat2Matr(rb.q);
    vec omega = MatrMulVec(MatrMulMatr(MatrMulMatr(rb.R, I_inv), Transpose(rb.R)), rb.L);
    res.q = QuatMulQuat({ 0, omega.x, omega.y, omega.z }, rb.q);
    res.q.r *= 0.5, res.q.i *= 0.5, res.q.j *= 0.5, res.q.k *= 0.5;
    res.l = vec{ 0, 0, 0 };
    res.L = vec{ 0, 0, 0 };

    return res;
}

void Solve(RigidBody& rb, double h) {
    RigidBody dt1 = f(rb), dt2 = f(dt1);

    rb.r.x = rb.r.x + h * dt1.r.x + h * h / 2 * dt2.r.x;
    rb.r.y = rb.r.y + h * dt1.r.y + h * h / 2 * dt2.r.y;
    rb.r.z = rb.r.z + h * dt1.r.z + h * h / 2 * dt2.r.z;

    rb.l.x = rb.l.x + h * dt1.l.x + h * h / 2 * dt2.l.x;
    rb.l.y = rb.l.y + h * dt1.l.y + h * h / 2 * dt2.l.y;
    rb.l.z = rb.l.z + h * dt1.l.z + h * h / 2 * dt2.l.z;

    rb.L.x = rb.L.x + h * dt1.L.x + h * h / 2 * dt2.L.x;
    rb.L.y = rb.L.y + h * dt1.L.y + h * h / 2 * dt2.L.y;
    rb.L.z = rb.L.z + h * dt1.L.z + h * h / 2 * dt2.L.z;

    rb.q.r = rb.q.r + h * dt1.q.r + h * h / 2 * dt2.q.r;
    rb.q.i = rb.q.i + h * dt1.q.i + h * h / 2 * dt2.q.i;
    rb.q.j = rb.q.j + h * dt1.q.j + h * h / 2 * dt2.q.j;
    rb.q.k = rb.q.k + h * dt1.q.k + h * h / 2 * dt2.q.k;

    rb.q = Normalize(rb.q);
    rb.R = Quat2Matr(rb.q);
}

RigidBody Add(RigidBody r1, RigidBody r2) {
    RigidBody res;

    res.r.x = r1.r.x + r2.r.x;
    res.r.y = r1.r.y + r2.r.y;
    res.r.z = r1.r.z + r2.r.z;

    res.q.r = r1.q.r + r2.q.r;
    res.q.i = r1.q.i + r2.q.i;
    res.q.j = r1.q.j + r2.q.j;
    res.q.k = r1.q.k + r2.q.k;

    res.l.x = r1.l.x + r2.l.x;
    res.l.y = r1.l.y + r2.l.y;
    res.l.z = r1.l.z + r2.l.z;

    res.L.x = r1.L.x + r2.L.x;
    res.L.y = r1.L.y + r2.L.y;
    res.L.z = r1.L.z + r2.L.z;

    return res;
}

RigidBody Multiply(RigidBody r, double num) {
    RigidBody res = r;

    res.r.x *= num;
    res.r.y *= num;
    res.r.z *= num;

    res.q.r *= num;
    res.q.i *= num;
    res.q.j *= num;
    res.q.k *= num;

    res.l.x *= num;
    res.l.y *= num;
    res.l.z *= num;

    res.L.x *= num;
    res.L.y *= num;
    res.L.z *= num;

    return res;
}

void Solve_2(RigidBody& rb, double h) {
    RigidBody k1, k2, k3, k4;

    k1 = f(rb);
    k2 = f(Add(rb, Multiply(k1, h / 2)));
    k3 = f(Add(rb, Multiply(k2, h / 2)));
    k4 = f(Add(rb, Multiply(k3, h)));
    rb = Add(rb, Multiply(Add(Add(Add(k1, Multiply(k2, 2)), Multiply(k3, 2)), k4), h / 6));

    rb.q = Normalize(rb.q);
    rb.R = Quat2Matr(rb.q);
}

double Rad2Deg(double rad) {
    return rad * 180 / pi;
}

void Display() {
    glViewport(0, 0, 500, 500);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluPerspective(60, 1, 1, 500);
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    Solve_2(rb, DeltaTime);
    vec omega = MatrMulVec(MatrMulMatr(MatrMulMatr(rb.R, I_inv), Transpose(rb.R)), rb.L);
    double Energy = 0.5 * Dot(omega, rb.L);
    printf("E = %.12le\n", Energy);
    glPushMatrix();
    glTranslated(rb.r.x, rb.r.y, rb.r.z - 200);
    glRotated(2 * Rad2Deg(acos(rb.q.r)), rb.q.i, rb.q.j, rb.q.k);
    DrawBlock(width, length, height);
    glPopMatrix();
    glFlush();
    glutSwapBuffers();
}

void Idle() {
    long Time;
    static long OldTime = -1, StartTime;
    static char Buf[100];

    if (OldTime == -1)
        StartTime = OldTime = clock();
    else {
        Time = clock();
        DeltaTime = (double)(Time - OldTime) / CLOCKS_PER_SEC;
        OldTime = clock();
    }
    glutPostRedisplay();
}

int main() {
    I_inv.m[0][0] = 1 / (1.0 / (12 * M_inv) * (length * length + height * height));
    I_inv.m[1][1] = 1 / (1.0 / (12 * M_inv) * (width * width + height * height));
    I_inv.m[2][2] = 1 / (1.0 / (12 * M_inv) * (width * width + length * length));
    rb.q = { cos(45), 1, 0, 0 };
    rb.R = Quat2Matr(rb.q);
    rb.L = vec{ 100, -200, 100 };

    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
    glutInitWindowSize(500, 500);
    glutInitWindowPosition(0, 0);
    glutCreateWindow("Window");
    glutDisplayFunc(Display);
    glutIdleFunc(Idle);
    glEnable(GL_DEPTH_TEST);
    glutMainLoop();

    return 0;
}