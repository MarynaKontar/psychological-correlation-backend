package com.psycorp.exception;

import com.psycorp.model.enums.ErrorEnum;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
    public class RestException extends RuntimeException {
        private int responseCode;
        private ErrorEnum error;
        private Map<String, Object> additionalData = new HashMap<>();

        public RestException(int responseCode) {
            super();
            this.responseCode = responseCode;
        }

        public RestException(String message, Throwable throwable) {
            super(message, throwable);
            responseCode = 500;
        }

        public RestException(String message, int responseCode) {
            super(message);
            this.responseCode = responseCode;
        }

        public RestException(String message, int responseCode, ErrorEnum error) {
            super(message);
            this.responseCode = responseCode;
            this.error = error;
        }

        public RestException(String message, int responseCode, ErrorEnum error, Throwable throwable) {
            super(message, throwable);
            this.responseCode = responseCode;
            this.error = error;
        }

        public RestException(String message, int responseCode, ErrorEnum error, Map<String, Object> additionalData) {
            super(message);
            this.responseCode = responseCode;
            this.error = error;
            this.additionalData = additionalData;
        }

        public RestException(String message, int responseCode, ErrorEnum error, Throwable throwable, Map<String, Object> additionalData) {
            super(message, throwable);
            this.responseCode = responseCode;
            this.error = error;
            this.additionalData = additionalData;
        }
    }

