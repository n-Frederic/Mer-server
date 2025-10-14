package com.example.demo.dto;

public class LoginResponseDTO {

    private boolean error;
    private String message;
    private String code;
    private UserData user;
    private String token;

    // 成功返回
    public static LoginResponseDTO success(String name, String email, String token) {
        LoginResponseDTO resp = new LoginResponseDTO();
        resp.error = false;
        resp.user = new UserData(name, email);
        resp.token = token;
        return resp;
    }

    // 失败返回
    public static LoginResponseDTO error(String message, String code) {
        LoginResponseDTO resp = new LoginResponseDTO();
        resp.error = true;
        resp.message = message;
        resp.code = code;
        return resp;
    }

    // Getter 和 Setter
    public boolean isError() { return error; }
    public void setError(boolean error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public UserData getUser() { return user; }
    public void setUser(UserData user) { this.user = user; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    // 内部静态类 UserData
    public static class UserData {
        private String name;
        private String email;

        public UserData(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
