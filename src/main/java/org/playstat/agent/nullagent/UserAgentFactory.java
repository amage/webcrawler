package org.playstat.agent.nullagent;

public class UserAgentFactory {
    static public String generateString() {
        return generateString(
                new AgentToken(AgentProductNames.Mozilla, "5.0", "X11; Linux x86_64"),
                new AgentToken(AgentProductNames.AppleWebKit, "537.22", "KHTML, like Gecko"),
                new AgentToken(AgentProductNames.UbuntuChromium, "25.0.1364.160", null),
                new AgentToken(AgentProductNames.Chrome, "25.0.1364.160", null), new AgentToken(
                        AgentProductNames.Safari, "537.22", null));
    }

    static public String generateString(AgentToken... tokens) {
        StringBuilder sb = new StringBuilder();
        for (AgentToken t : tokens) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(t.getProduct());
            if (t.isVersionSet()) {
                sb.append("/");
                sb.append(t.getVersion());
            }
            if (t.isCommentSet()) {
                sb.append(" (");
                sb.append(t.getComment());
                sb.append(")");
            }
        }
        return sb.toString();
    }

    private enum AgentProductNames {
        Mozilla("Mozilla"), Safari("Safari"), AppleWebKit("AppleWebKit"), UbuntuChromium(
                "Ubuntu Chromium"), Chrome("Chrome");
        private final String name;

        private AgentProductNames(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class AgentToken {
        private final String product;
        private final String version;
        private final String comment;

        public AgentToken(String product, String version, String comment) {
            this.product = product;
            this.version = version;
            this.comment = comment;
        }

        public AgentToken(AgentProductNames product, String version, String comment) {
            this.product = product.getName();
            this.version = version;
            this.comment = comment;
        }

        public String getProduct() {
            return product;
        }

        public String getVersion() {
            return version;
        }

        public boolean isVersionSet() {
            if (version != null && version.length() > 0) {
                return true;
            }
            return false;
        }

        public String getComment() {
            return comment;
        }

        public boolean isCommentSet() {
            if (comment != null && comment.length() > 0) {
                return true;
            }
            return false;
        }
    }
}
