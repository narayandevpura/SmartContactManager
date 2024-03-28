package com.smart.jwt.model;


public class JwtResponse {

    private String token;
    private String userName;

    public JwtResponse() {
    }

    public JwtResponse(String token, String userName) {
        this.token = token;
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public String getUserName() {
        return userName;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public static class builder {
        private String token;
        private String userName;

        public builder withToken(String token) {
            this.token = token;
            return this;
        }

        public builder withUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public JwtResponse build() {
            return new JwtResponse(token, userName);
        }
    }

}
