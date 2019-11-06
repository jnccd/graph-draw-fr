package frLayouting2;

import org.eclipse.elk.graph.ElkShape;

public class Vector2 {
    public double X, Y;
    
    public Vector2(double a) {
        X = a;
        Y = a;
    }
    public Vector2(double x, double y) {
        X = x;
        Y = y;
    }
    
    public Vector2 Add(Vector2 otherBoi) {
        return new Vector2(X + otherBoi.X, Y + otherBoi.Y);
    }
    public Vector2 Sub(Vector2 otherBoi) {
        return new Vector2(X - otherBoi.X, Y - otherBoi.Y);
    }
    public Vector2 Mult(Vector2 otherBoi) {
        return new Vector2(X * otherBoi.X, Y * otherBoi.Y);
    }
    public Vector2 Div(Vector2 otherBoi) {
        return new Vector2(X / otherBoi.X, Y / otherBoi.Y);
    }
    
    public Vector2 Add(double otherBoi) {
        return new Vector2(X + otherBoi, Y + otherBoi);
    }
    public Vector2 Sub(double otherBoi) {
        return new Vector2(X - otherBoi, Y - otherBoi);
    }
    public Vector2 Mult(double otherBoi) {
        return new Vector2(X * otherBoi, Y * otherBoi);
    }
    public Vector2 Div(double otherBoi) {
        return new Vector2(X / otherBoi, Y / otherBoi);
    }
    
    public double Length() {
        return Math.sqrt(LengthSquared());
    }
    public double LengthSquared() {
        return X * X + Y * Y;
    }
    
    public static Vector2 FromELK(ElkShape e) { return new Vector2(e.getX(), e.getY()); }
    public static Vector2 Zero() { return new Vector2(0, 0); }
    public static Vector2 One() { return new Vector2(1, 1); }
}
