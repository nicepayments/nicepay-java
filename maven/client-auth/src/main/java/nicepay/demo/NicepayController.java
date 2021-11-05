package nicepay.demo;

import java.util.Enumeration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

@Controller
public class NicepayController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String CLIENT_ID = "S1_6eaa0db1afdc41f3becb770878d67d25"; 
    private final String SECRET_KEY = "e80d068e400649a6ada66777fa350d40";

    @RequestMapping("/")
    public String indexDemo(Model model){
        UUID id = UUID.randomUUID();
        model.addAttribute("orderId", id);
        model.addAttribute("clientId", CLIENT_ID);        
        return "/index";
    }

    @RequestMapping(value="/cancel")
    public String cancelDemo(){
        return "/cancel";
    }

    @RequestMapping("/clientAuth")
    public String main(HttpServletRequest request, Model model){ 
        String resultMsg = request.getParameter("resultMsg");
        String resultCode = request.getParameter("resultCode"); 
        model.addAttribute("resultMsg", resultMsg);

        if (resultCode.equalsIgnoreCase("0000")) {
            // 결제 성공 비즈니스 로직 구현
        } else {
            // 결제 실패 비즈니스 로직 구현
        }

        //응답 request body 로그 확인
        Enumeration<String> params = request.getParameterNames(); 
        while(params.hasMoreElements()){
            String paramName = params.nextElement();
            System.out.println(paramName+" : "+request.getParameter(paramName));
        }        

        return "/response"; 
    }

    @RequestMapping("/cancelAuth")
    public String requestCancel(
            @RequestParam String tid,
            @RequestParam String amount,
            Model model) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + SECRET_KEY).getBytes()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> AuthenticationMap = new HashMap<>();
        AuthenticationMap.put("amount", amount);
        AuthenticationMap.put("reason", "test");
        AuthenticationMap.put("orderId", UUID.randomUUID().toString());

        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(AuthenticationMap), headers);

        ResponseEntity<JsonNode> responseEntity = restTemplate.postForEntity(
            "https://sandbox-api.nicepay.co.kr/v1/payments/"+ tid +"/cancel", request, JsonNode.class);

        JsonNode responseNode = responseEntity.getBody();
        String resultCode = responseNode.get("resultCode").asText();
        model.addAttribute("resultMsg", responseNode.get("resultMsg").asText());

        System.out.println(responseNode.toPrettyString());

        if (resultCode.equalsIgnoreCase("0000")) {
            // 취소 성공 비즈니스 로직 구현
        } else {
            // 취소 실패 비즈니스 로직 구현
        }

        return "/response";
    }    

    @RequestMapping("/hook")
    public ResponseEntity<String> hook(@RequestBody HashMap<String, Object> hookMap) throws Exception {
        String resultCode = hookMap.get("resultCode").toString();

        System.out.println(hookMap);

        if(resultCode.equalsIgnoreCase("0000")){
            return ResponseEntity.status(HttpStatus.OK).body("ok");
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}