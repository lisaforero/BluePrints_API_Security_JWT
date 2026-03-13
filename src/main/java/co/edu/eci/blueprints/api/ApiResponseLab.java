package co.edu.eci.blueprints.api;

public record ApiResponseLab<T>(int code, String message, T data) {

}