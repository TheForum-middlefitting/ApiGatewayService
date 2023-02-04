//package com.example.apigatewayservice.filter;
//
//@Component
//public class JwtRequestFilter extends
//        AbstractGatewayFilterFactory<JwtRequestFilter.Config> implements Ordered {
//
//    final Logger logger =
//            LoggerFactory.getLogger(JwtRequestFilter.class);
//
//    @Autowired
//    private JwtValidator jwtValidator;
//
//    @Override
//    public int getOrder() {
//        return -2; // -1 is response write filter, must be called before that
//    }
//
//    public static class Config {
//        private String role;
//        public Config(String role) {
//            this.role = role;
//        }
//        public String getRole() {
//            return role;
//        }
//    }
//
//    @Bean
//    public ErrorWebExceptionHandler myExceptionHandler() {
//        return new MyWebExceptionHandler();
//    }
//
//    public class MyWebExceptionHandler implements ErrorWebExceptionHandler {
//        private String errorCodeMaker(int errorCode) {
//            return "{\"errorCode\":" + errorCode +"}";
//        }
//
//        @Override
//        public Mono<Void> handle(
//                ServerWebExchange exchange, Throwable ex) {
//            logger.warn("in GATEWAY Exeptionhandler : " + ex);
//            int errorCode = 999;
//            if (ex.getClass() == NullPointerException.class) {
//                errorCode = 61;
//            } else if (ex.getClass() == ExpiredJwtException.class) {
//                errorCode = 56;
//            } else if (ex.getClass() == MalformedJwtException.class || ex.getClass() == SignatureException.class || ex.getClass() == UnsupportedJwtException.class) {
//                errorCode = 55;
//            } else if (ex.getClass() == IllegalArgumentException.class) {
//                errorCode = 51;
//            }
//
//            byte[] bytes = errorCodeMaker(errorCode).getBytes(StandardCharsets.UTF_8);
//            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
//            return exchange.getResponse().writeWith(Flux.just(buffer));
//        }
//    }
//
//
//    public JwtRequestFilter() {
//        super(Config.class);
//    }
//    // public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//    @Override
//    public GatewayFilter apply(Config config) {
//        return (exchange, chain) -> {
//            String token = exchange.getRequest().getHeaders().get("Authorization").get(0).substring(7);
//            logger.info("token : " + token);
//            Map<String, Object> userInfo = jwtValidator.getUserParseInfo(token);
//            ArrayList<String> arr = (ArrayList<String>)userInfo.get("role");
//            if ( !arr.contains(config.getRole())) {
//                throw new IllegalArgumentException();
//            }
//            return chain.filter(exchange);
//        };
//    }
//}