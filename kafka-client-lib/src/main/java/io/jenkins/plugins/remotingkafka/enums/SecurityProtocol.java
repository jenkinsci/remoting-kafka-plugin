package io.jenkins.plugins.remotingkafka.enums;

/**
 * Protocol used to communicate with brokers
 */
public enum SecurityProtocol {
    PLAINTEXT {
        @Override
        public String toString() {
            return "PLAINTEXT";
        }
    },
    SSL {
        @Override
        public String toString() {
            return "SSL";
        }
    },
    SASL_PLAINTEXT {
        @Override
        public String toString() {
            return "SASL_PLAINTEXT";
        }
    },
    SASL_SSL {
        @Override
        public String toString() {
            return "SASL_SSL";
        }
    }
}
