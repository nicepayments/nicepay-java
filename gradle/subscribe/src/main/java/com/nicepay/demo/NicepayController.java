package com.nicepay.demo;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class NicepayController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String CLIENT_ID = "클라이언트 키";
    private final String SECRET_KEY = "시크릿 키";

    @RequestMapping("/")
    public String indexDemo(Model model){
        return "/regist";
    }

    @RequestMapping("/regist")
    public String requestPayment(
            @RequestParam String cardNo,
            @RequestParam String expYear,
            @RequestParam String expMonth,
            @RequestParam String idNo,
            @RequestParam String cardPw,
            Model model) throws Exception {

        StringBuffer requestData = new StringBuffer();
        requestData.append("cardNo=").append(String.valueOf(cardNo)).append("&");
        requestData.append("expYear=").append(String.valueOf(expYear)).append("&");
        requestData.append("expMonth=").append(String.valueOf(expMonth)).append("&");
        requestData.append("idNo=").append(String.valueOf(idNo)).append("&");
        requestData.append("cardPw=").append(String.valueOf(cardPw));

        String encrypt = encrypt(requestData.toString(), SECRET_KEY.substring(0, 32), SECRET_KEY.substring(0, 16));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + SECRET_KEY).getBytes()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> AuthenticationMap = new HashMap<>();
        AuthenticationMap.put("encData", encrypt);
        AuthenticationMap.put("orderId", UUID.randomUUID().toString() );
        AuthenticationMap.put("encMode", "A2");

        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(AuthenticationMap), headers);

        ResponseEntity<JsonNode> responseEntity = restTemplate.postForEntity(
            "https://api.nicepay.co.kr/v1/subscribe/regist", request, JsonNode.class);

        JsonNode responseNode = responseEntity.getBody();
        String resultCode = responseNode.get("resultCode").asText();
        model.addAttribute("resultMsg", responseNode.get("resultMsg").asText());

        String bid = responseNode.get("bid").asText();

        billing(bid);
        expire(bid);

        System.out.println(responseNode.toPrettyString());

        if (resultCode.equalsIgnoreCase("0000")) {
            // 성공 비즈니스 로직 구현
        } else {
            // 실패 비즈니스 로직 구현
        }
        
        return "response";
    }

    public void billing(String bid) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + SECRET_KEY).getBytes()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> AuthenticationMap = new HashMap<>();
        AuthenticationMap.put("orderId", UUID.randomUUID().toString() );
        AuthenticationMap.put("amount", 1004);
        AuthenticationMap.put("goodsName", "card billing test");
        AuthenticationMap.put("cardQuota", 0);
        AuthenticationMap.put("useShopInterest", false);

        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(AuthenticationMap), headers);

        ResponseEntity<JsonNode> responseEntity = restTemplate.postForEntity(
            "https://api.nicepay.co.kr/v1/subscribe/"+bid+"/payments", request, JsonNode.class);

        JsonNode responseNode = responseEntity.getBody();
        String resultCode = responseNode.get("resultCode").asText();

        System.out.println(responseNode.toPrettyString());

        if (resultCode.equalsIgnoreCase("0000")) {
            // 성공 비즈니스 로직 구현
        } else {
            // 실패 비즈니스 로직 구현
        }
    }

    public void expire(String bid) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + SECRET_KEY).getBytes()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> AuthenticationMap = new HashMap<>();
        AuthenticationMap.put("orderId", UUID.randomUUID().toString() );

        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(AuthenticationMap), headers);

        ResponseEntity<JsonNode> responseEntity = restTemplate.postForEntity(
            "https://api.nicepay.co.kr/v1/subscribe/"+bid+"/expire", request, JsonNode.class);

        JsonNode responseNode = responseEntity.getBody();
        String resultCode = responseNode.get("resultCode").asText();

        System.out.println(responseNode.toPrettyString());

        if (resultCode.equalsIgnoreCase("0000")) {
            // 성공 비즈니스 로직 구현
        } else {
            // 실패 비즈니스 로직 구현
        }
    }    

    //hex(aec-256-cbc-PKCS5Padding)
    public String encrypt(String str, String secretKey, String iv) throws Exception {
        SecretKey secureKey = new SecretKeySpec(secretKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(iv.getBytes()));
        byte[] encrypted = cipher.doFinal(str.getBytes("UTF-8"));
        return Hex.encodeHexString( encrypted );
    }

}