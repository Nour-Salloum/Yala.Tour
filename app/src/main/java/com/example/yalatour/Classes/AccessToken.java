package com.example.yalatour.Classes;

import com.google.api.client.util.Lists;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.ImmutableList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;



public class AccessToken {
    private static final  String firebaseMessagingScope="https://www.googleapis.com/auth/firebase.messaging";

    public String getAccessToken(){
        try {
            String jsonString=
                    "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"yala-tour\",\n" +
                    "  \"private_key_id\": \"78d1a900bdc360e6439a86688869c59ce43c7118\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCnL+v2h10CTUAJ\\nXozUszxN8rrIWLSJ1ugHnJCLVrXT1jC6PQg3kFr8sYf3qumq/VaQF83H7wlt9LIp\\nXh7oWbFukybnCMB+TXMxGwfcpjBEHHtGPi9em80bU978a8IkH63R3AWFEpGpZWAf\\n9h02Jj9hsVlB8Vnhj+btKkxzvtoDvGrnaCDjha9TN8xdgUw2pxXFZNMnolMxaF3Y\\n72UHddup7N8j1qlpyZST2cnPPxjrHPDpF5wSxdYwggR0UntnKqGrLqb7LIFW+11l\\n8pM948wNFCU8YwB6xDXb9EtCIzBiDHNKeAED17dkzM9wmEso/L2dF7neE3WxmnpX\\n9Shs2WbrAgMBAAECggEABLfX/2WTjOCUOY93Bvul7Ic4mTXTpn4vnyNeeugwCnHl\\nfyQAytyijLYP4b/rhz1OB5P8ZTOMPxOkQYGb+q+pT7cMZoG7OPVDlnOuVZ5FB28M\\nVY+ks5VPBQHpbZM27+EK/Typ1TcQcEjhnRVLfr82k7G3FKLUiAaRHElQzgVpTqij\\nVzZV/iAJTIB2PC2lM9NuIIhvCjbj60TPK2gb5uUOr43NvGJk5czDQqiTAthM2qpG\\nDkQsr5Ofr5nrYgvxN1B+cWKdukXXw1NZT8n06K+4au05evCp/SJmwEQZOr88vTZ5\\nrdEOfkqe1JohxO3yZlsBtSCoKm1gpCc947SjMM/caQKBgQDmToqjVD0d4RjaRWQ8\\nsW6oUqkXsnUYEran727be1tTa+pJUkU1Q4oPbKA2ath1/ugAl0WyTaWFRc+9115D\\nG4drh8XkHBeZCvCpMHQeXKNUZsNM5lCR3pkbpclxMwsEpPwp4QQBohOdSxj5b9e5\\nVYZXJWBKp93fJn6hnNGi+mVrAwKBgQC51rZtySMjH4UQUZKXlkvhp0DyhtWYY97X\\nuZJZp6dtpqdl+E59mqIzxpB6NjI37pyDLFi8nJjFSxahJEeUPLqRcLTOGTTgYk+v\\nah/AtkF9uDiQ5BEOyPYJKczMg/28QGP5XgBO7Dw638fa1shGfGlzHTYdDOLgCbTK\\nRhq/CPkb+QKBgHoVxo5gWGmfaqJCSKmX0TYvY22pb0MMoIETfqugj8AAI9ksYH2k\\nyOn2RlOyff7G2st9ZTfoy0aDxGU5ZsGiNT4+YhvS/Htnfj3h7pwttXNHal44dRtu\\nabFrSAbW7Q9QOlAk9Q+8kDNAnQOVU0Du4mEN8iaBKC9j45reW47Ot7ZrAoGBAJ7X\\n8Mx8HKV73o+pOcSJ+cgh3riuGUoNKz925anuTITD6teU2vovPy9ELN/ZDSmTsYHJ\\nWPu2WjpgyTHpQalqEwoXrONw1ipovbWw3v0oi2f7G5Z06kOZ7/q7GMtCkpHi89EE\\n0PyknYKxHjFmXn8o/jUfpc5E4nMeuMIgE+JvuAV5AoGAewcHfMQRHVMJeCWwid1P\\nMeAdxh4Lz83HECTM+xmOMaqurIOQiiEzaLGQj9uv0sfnvemL7PQJNZ6vELgFrVRP\\n13E3Ckoxq8imPe0pm+gZLIRy7DA8JpEcsRkfe7vwwFpj8suBkJatXIj4LSf56GC6\\n/m+PpMK4+CXdCTYrcK7Vdpg=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-4n91t@yala-tour.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"104399890744273936760\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-4n91t%40yala-tour.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n";
            InputStream stream =new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream).createScoped(ImmutableList.of(firebaseMessagingScope));
            googleCredentials.refresh();
            return googleCredentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
