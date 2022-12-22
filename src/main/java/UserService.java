import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UserService {
    //CustomerType
    public static final int CUSTOMER_TYPE_SPONSOR = 1;
    public static final int CUSTOMER_TYPE_MINOR = 2;

    //Relationship enum
    public static final int RELATIONSHIP_ENUM_PARENT = 1;
    public static final int RELATIONSHIP_ENUM_CHILD = 2;
    public static final int RELATIONSHIP_ENUM_OTHER = 3;

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    /**
     * Creates new user via POST method
     * @param firstName - user first name
     * @param lastName - user last name
     * @param address - user address
     * @param dateOfBirth - user date of birth
     * @param customerType - customer type
     * @param relationshipEnum - relationship enum
     * @throws CustomException if status code is not 201
     */
    public void createUser(String firstName, String lastName, String address, int dateOfBirth, int customerType, int relationshipEnum) throws CustomException {
        Map<Object, Object> params = new HashMap<>();
        params.put("firstName", firstName);
        params.put("lastName", lastName);
        params.put("email", generateEmail(firstName, lastName));
        params.put("ssn", getRandomSSN());
        params.put("address", address);
        params.put("dateOfBirth", parseDate(dateOfBirth));
        params.put("customerType", customerType);
        params.put("relationshipEnum", relationshipEnum);

        HttpService httpService = new HttpService();
        HttpResponse<String> response = httpService.sendPostRequest("createUser", params);
        if (response.statusCode() != HttpURLConnection.HTTP_CREATED) {
            throw new CustomException("Fail to create new user. Response status is: " + response.statusCode() +
                    " with response body: " + response.body());
        }
    }

    /**
     * Get user data by user db id
     * @param userId - user database id
     * @return - Map with user information
     * @throws CustomException
     */
    public Map<Object, Object> getUserById(int userId) throws CustomException {
        HttpService httpService = new HttpService();
        HttpResponse<String> response = httpService.sendGet("user/" + userId);
        //Parse body response and return Map<Object, Object> with user data
        return new HashMap<>();
    }

    /**
     * Get user data by first name and last name
     * @param firstName - user first name
     * @param lastName - user last name
     * @return - Map with user information
     * @throws CustomException
     */
    public HashMap<Object, Object> getUserByName(String firstName, String lastName) throws CustomException {
        HttpService httpService = new HttpService();
        String searchParams = "fistName=" + firstName + "&lastName=" + lastName;
        HttpResponse<String> response = httpService.sendGet("user?" + searchParams);
        //Parse body response and return Map<Object, Object> with user data
        return new HashMap<>();
    }

    /**
     * Generates random 8 digit integer value
     * @return 8 digit int value
     */
    private int getRandomSSN() {
        Random random = new Random();
        return random.nextInt(90000000) + 10000000;
    }

    /**
     * Creates email from combination of user first and last name
     * @param firstName - user first name
     * @param lastName - user last name
     * @return String email
     */
    private String generateEmail(String firstName, String lastName) {
        return firstName + lastName + "@example.com";
    }

    /**
     * Parse int date to LocalDate with MM/DD/YYYY format
     *
     * @param numberDate parameter with date in format yyyymmdd
     * @return parsed LocalDate
     */
    private static String parseDate(int numberDate) throws CustomException {
        String intDateToString = String.valueOf(numberDate);
        SimpleDateFormat intDateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date;
        try {
            date = intDateFormat.parse(intDateToString);
        } catch (ParseException e) {
            throw new CustomException("Fail to parse int date: ", e);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        return dateFormat.format(date);
    }
}
