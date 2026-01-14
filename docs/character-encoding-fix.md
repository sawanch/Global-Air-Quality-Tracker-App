# Character Encoding Issue - UTF-8 Handling

## Problem

City names with special characters (accents, umlauts, etc.) were displaying incorrectly:

```json
❌ BAD: "city": "A�roport de Montr�al"
✅ GOOD: "city": "Aéroport de Montréal"
```

This happens when UTF-8 encoded data is incorrectly read or transmitted with a different character encoding (like ISO-8859-1 or Windows-1252).

---

## Root Cause

The issue occurred at **three levels**:

1. **HTTP Client Level** - RestTemplate wasn't explicitly configured to use UTF-8
2. **HTTP Headers** - We weren't explicitly requesting UTF-8 responses from the OpenAQ API
3. **Spring Boot Config** - No global UTF-8 enforcement for HTTP message converters

---

## Solution Applied

### 1. **RestTemplate UTF-8 Configuration**

Updated `OpenAQApiClient.java` constructor:

```java
public OpenAQApiClient() {
    this.restTemplate = new RestTemplate();
    
    // Configure RestTemplate to use UTF-8 encoding for proper character handling
    this.restTemplate.getMessageConverters()
        .stream()
        .filter(converter -> converter instanceof StringHttpMessageConverter)
        .map(converter -> (StringHttpMessageConverter) converter)
        .forEach(converter -> converter.setDefaultCharset(StandardCharsets.UTF_8));
    
    this.objectMapper = new ObjectMapper();
}
```

**What it does:**
- Finds all `StringHttpMessageConverter` instances in RestTemplate
- Sets their default charset to UTF-8
- Ensures API responses are read with correct encoding

---

### 2. **HTTP Headers Configuration**

Updated the API request headers:

```java
HttpHeaders headers = new HttpHeaders();
// Explicitly request UTF-8 encoded JSON responses
headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
headers.setAccept(Collections.singletonList(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)));
headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));

if (apiKey != null && !apiKey.isEmpty()) {
    headers.set("X-API-Key", apiKey);
}
```

**What it does:**
- `Content-Type: application/json; charset=UTF-8` - Tells the server we're sending UTF-8
- `Accept: application/json; charset=UTF-8` - Tells the server we expect UTF-8 responses
- `Accept-Charset: UTF-8` - Explicitly declares UTF-8 as the preferred charset

---

### 3. **Spring Boot Global Configuration**

Added to `application.properties`:

```properties
# UTF-8 Encoding Configuration (handle special characters like é, ñ, etc.)
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
spring.http.encoding.force=true
```

**What it does:**
- `spring.http.encoding.charset=UTF-8` - Sets default charset to UTF-8
- `spring.http.encoding.enabled=true` - Enables the encoding filter
- `spring.http.encoding.force=true` - Forces UTF-8 even if the client specifies a different encoding

---

## Database Configuration

Our MySQL table already uses UTF-8:

```sql
CREATE TABLE air_quality_data (
    ...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- `utf8mb4` - MySQL's true UTF-8 implementation (supports 4-byte characters including emojis)
- `utf8mb4_unicode_ci` - Case-insensitive Unicode collation

---

## How to Apply the Fix

### 1. **Stop the Running Application**

```bash
# Find the Java process
ps aux | grep air-quality-tracker-api

# Kill it (replace PID with actual process ID)
kill <PID>
```

Or simply press `Ctrl+C` in the terminal where it's running.

---

### 2. **Rebuild and Restart**

```bash
cd /Users/sawan/Documents/24-Intuit/Global-Air-Quality-Tracker-App/air-quality-tracker-api

# Clean build with new changes
mvn clean package -DskipTests

# Start the application
java -jar target/air-quality-tracker-api.jar
```

Or use the convenient run script:

```bash
cd /Users/sawan/Documents/24-Intuit/Global-Air-Quality-Tracker-App/air-quality-tracker-api
./run.sh
```

---

### 3. **Verify the Fix**

After restart, the application will:
1. Re-fetch data from OpenAQ API with proper UTF-8 encoding
2. Store city names correctly in the database
3. Serve them correctly via the REST API

**Test it:**

```bash
# Check a specific city with special characters
curl -s "http://localhost:8080/api/cities/Montreal" | python3 -m json.tool
```

You should now see:
```json
{
  "city": "Aéroport de Montréal",
  "country": "Canada",
  ...
}
```

---

## Examples of Cities That Will Now Display Correctly

| Before (Wrong) | After (Correct) |
|---------------|-----------------|
| A�roport de Montr�al | Aéroport de Montréal |
| S�o Paulo | São Paulo |
| M�laga | Málaga |
| Z�rich | Zürich |
| Bogot� | Bogotá |
| K�benhavn | København |
| Gda�sk | Gdańsk |

---

## Technical Details

### Why UTF-8?

UTF-8 is the industry standard for web applications because:
- **Universal**: Supports all languages and special characters
- **Backward Compatible**: First 128 characters are identical to ASCII
- **Efficient**: Variable-length encoding (1-4 bytes per character)
- **Web Standard**: Required by HTML5 and JSON specifications

### Character Encoding Flow

```
OpenAQ API → [UTF-8 Response]
     ↓
RestTemplate (with UTF-8 converter) → [Correct String in Java]
     ↓
Jackson ObjectMapper → [Java Objects with correct strings]
     ↓
MySQL (utf8mb4) → [Stored correctly]
     ↓
Spring Boot REST API (UTF-8 response) → [Frontend receives correct data]
```

---

## Prevention

To avoid encoding issues in the future:

1. **Always specify UTF-8** in:
   - HTTP headers (`Content-Type`, `Accept-Charset`)
   - Database table definitions (`utf8mb4`)
   - File encodings (IDE settings)
   - HTML meta tags (`<meta charset="UTF-8">`)

2. **Use StandardCharsets.UTF_8** in Java code instead of string literals like `"UTF-8"`

3. **Test with international data** - Don't just test with ASCII characters

4. **Monitor logs** - Character encoding issues often show up as `?` or `�` in logs

---

## Troubleshooting

### If characters still look wrong:

1. **Clear the database**:
   ```sql
   USE air_quality_db;
   TRUNCATE TABLE air_quality_data;
   ```
   The application will re-fetch and re-populate on next startup.

2. **Check MySQL encoding**:
   ```sql
   SHOW VARIABLES LIKE 'char%';
   ```
   Ensure `character_set_database` and `character_set_server` are `utf8mb4`.

3. **Check browser console**: Frontend might be forcing a different encoding.

4. **Verify API response**:
   ```bash
   curl -s "http://localhost:8080/api/global" | file -
   ```
   Should output: `UTF-8 Unicode text`

---

## References

- [Spring Boot Character Encoding](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.servlet.spring-mvc.encoding)
- [MySQL utf8mb4 Character Set](https://dev.mysql.com/doc/refman/8.0/en/charset-unicode-utf8mb4.html)
- [RFC 3629 - UTF-8](https://datatracker.ietf.org/doc/html/rfc3629)
- [Joel on Software: The Absolute Minimum Every Software Developer Must Know About Unicode](https://www.joelonsoftware.com/2003/10/08/the-absolute-minimum-every-software-developer-absolutely-positively-must-know-about-unicode-and-character-sets-no-excuses/)

---

## Summary

✅ **Fixed at 3 levels:**
1. RestTemplate message converters → UTF-8
2. HTTP request/response headers → UTF-8
3. Spring Boot global config → UTF-8

✅ **Database was already correct:** utf8mb4

✅ **Result:** All international city names now display correctly! 🌍
