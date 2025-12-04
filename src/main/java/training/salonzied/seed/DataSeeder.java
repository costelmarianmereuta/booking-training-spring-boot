package training.salonzied.seed;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class DataSeeder {
    private static final String BASE_URL = "http://localhost:8080";

    public static void main(String[] args) {

        RestTemplate rest = new RestTemplate();

        // 1. Creare salon
        System.out.println("=== Create Salon ===");

        Map<String, Object> salonRequest = Map.of(
                "name", "Salon Prestige",
                "address", Map.of(
                        "street", "Rue de Paris",
                        "houseNumber", "12",
                        "postalBox", "Boite 3",
                        "postcode", "1000",
                        "city", "Bruxelles"
                )
        );

        ResponseEntity<Map> salonResponse = rest.exchange(
                BASE_URL + "/api/v1/salons",
                HttpMethod.POST,
                new HttpEntity<>(salonRequest, jsonHeaders()),
                Map.class
        );

        String salonPublicId = (String) salonResponse.getBody().get("publicId");
        System.out.println("Salon publicId = " + salonPublicId);



        // 2. Creare user
        System.out.println("=== Create User ===");

        Map<String, Object> userRequest = Map.of(
                "firstName", "Maria",
                "lastName", "Popescu",
                "email", "maria.popescu@example.com",
                "phone", "+32 470 12 34 56",
                "birthDate", "1995-03-21",
                "gender", "female"
        );

        ResponseEntity<Map> userResponse = rest.exchange(
                BASE_URL + "/api/v1/user",
                HttpMethod.POST,
                new HttpEntity<>(userRequest, jsonHeaders()),
                Map.class
        );

        String userPublicId = (String) userResponse.getBody().get("publicId");
        System.out.println("User publicId = " + userPublicId);



        // 3. Creare categorie
        System.out.println("=== Create Category ===");

        Map<String, Object> categoryRequest = Map.of(
                "name", "Epilation",
                "description", "Hair removal",
                "salonPublicId", salonPublicId
        );

        rest.exchange(
                BASE_URL + "/api/v1/treatments/category",
                HttpMethod.POST,
                new HttpEntity<>(categoryRequest, jsonHeaders()),
                Map.class
        );


        // 4. Creare treatment
        System.out.println("=== Create Treatment ===");

        Map<String, Object> treatmentRequest = Map.of(
                "name", "Facial massage",
                "price", 75.0,
                "duration", 60,
                "timeBetweenTreatments", 15,
                "description", "Relaxing massage",
                "categoryName", "Epilation"
        );

        ResponseEntity<Map> treatmentResponse = rest.exchange(
                BASE_URL + "/api/v1/treatments",
                HttpMethod.POST,
                new HttpEntity<>(treatmentRequest, jsonHeaders()),
                Map.class
        );

        String treatmentName = treatmentResponse.getBody().get("name").toString();
        System.out.println("Treatment name = " + treatmentName);



        // 5. Creare rezervare
        System.out.println("=== Create Reservation ===");

        Map<String, Object> reservationRequest = new java.util.HashMap<>();
        reservationRequest.put("salonPublicId", salonPublicId);
        reservationRequest.put("userPublicId", userPublicId);
// deocamdată punem un id hardcodat
        reservationRequest.put("treatmentName", "Facial massage");
        reservationRequest.put("startTime", "2025-11-20T10:30:00Z");
        reservationRequest.put("notes", "Allergic to strong oils");


        ResponseEntity<Map> reservationResponse = rest.exchange(
                BASE_URL + "/api/v1/reservations",
                HttpMethod.POST,
                new HttpEntity<>(reservationRequest, jsonHeaders()),
                Map.class
        );

        System.out.println("Reservation created:");
        System.out.println(reservationResponse.getBody());
    }


    private static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
