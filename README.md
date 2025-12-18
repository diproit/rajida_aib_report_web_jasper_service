# Rajida Report Service

A Spring Boot application for uploading JasperReports JRXML files and exporting them to PDF and HTML formats with **data-driven report support**.

## Features

- ✅ Upload JRXML files
- ✅ Export reports to PDF (with and without data)
- ✅ Export reports to HTML (with and without data)
- ✅ **Data-driven reports** - Send data to populate templates
- ✅ List uploaded files
- ✅ Delete JRXML files
- ✅ Health check endpoint
- ✅ CORS enabled for frontend integration

## Prerequisites

- Java 17+
- Maven 3.9.6+
- Docker (optional, for containerized deployment)

## Building the Project

```bash
mvn clean package
```

This will create a fat JAR file at `target/rajida-report-service-1.0.0.jar`

## Running the Application

### Option 1: Run with Maven
```bash
mvn spring-boot:run
```

### Option 2: Run the JAR file
```bash
java -jar target/rajida-report-service-1.0.0.jar
```

### Option 3: Run with Docker
```bash
docker build -t rajida-report-service:1.0.0 .
docker run -p 8080:8080 -v $(pwd)/uploads:/app/uploads rajida-report-service:1.0.0
```

## API Endpoints

Base URL: `http://localhost:8080/api/reports`

### 1. Health Check
```
GET /api/reports/health
```
**Response:**
```json
{
  "success": true,
  "message": "Service is running",
  "data": null
}
```

### 2. Upload JRXML File
```
POST /api/reports/upload
Content-Type: multipart/form-data
```
**Request:**
- `file` (required): The JRXML file to upload

**Response:**
```json
{
  "success": true,
  "message": "JRXML file uploaded successfully",
  "data": {
    "fileName": "1702518000000_report.jrxml",
    "message": "File uploaded successfully"
  }
}
```

**Example with cURL:**
```bash
curl -X POST -F "file=@path/to/your/report.jrxml" \
  http://localhost:8080/api/reports/upload
```

### 3. Export to PDF
```
POST /api/reports/export-pdf
Content-Type: application/json
```
**Request Body:**
```json
{
  "jrxmlFileName": "1702518000000_report.jrxml",
  "parameters": {
    "param1": "value1",
    "param2": "value2"
  },
  "exportFormat": "pdf"
}
```

**Response:**
- Binary PDF file

**Example with cURL:**
```bash
curl -X POST http://localhost:8080/api/reports/export-pdf \
  -H "Content-Type: application/json" \
  -d '{
    "jrxmlFileName": "1702518000000_report.jrxml",
    "parameters": {}
  }' \
  --output report.pdf
```

### 4. Export to HTML
```
POST /api/reports/export-html
Content-Type: application/json
```
**Request Body:**
```json
{
  "jrxmlFileName": "1702518000000_report.jrxml",
  "parameters": {
    "param1": "value1",
    "param2": "value2"
  },
  "exportFormat": "html"
}
```

**Response:**
- HTML content

**Example with cURL:**
```bash
curl -X POST http://localhost:8080/api/reports/export-html \
  -H "Content-Type: application/json" \
  -d '{
    "jrxmlFileName": "1702518000000_report.jrxml",
    "parameters": {}
  }' \
  --output report.html
```

### 5. Export to PDF WITH DATA ⭐ NEW
```
POST /api/reports/export-pdf-with-data
Content-Type: application/json
```
**Request Body:**
```json
{
  "jrxmlFileName": "sales-report.jrxml",
  "parameters": {
    "ReportTitle": "Sales Report",
    "CompanyName": "Rajida Holdings"
  },
  "dataRecords": [
    {
      "productName": "Laptop",
      "quantity": 5,
      "price": 1500.00,
      "total": 7500.00
    },
    {
      "productName": "Monitor",
      "quantity": 10,
      "price": 300.00,
      "total": 3000.00
    }
  ],
  "exportFormat": "pdf"
}
```

**Response:**
- Binary PDF file with populated data

**Example with cURL:**
```bash
curl -X POST http://localhost:8080/api/reports/export-pdf-with-data \
  -H "Content-Type: application/json" \
  -d '{
    "jrxmlFileName": "sales-report.jrxml",
    "parameters": {"ReportTitle": "My Report"},
    "dataRecords": [
      {"productName": "Laptop", "quantity": 5, "price": 1500}
    ]
  }' \
  --output report.pdf
```

### 6. Export to HTML WITH DATA ⭐ NEW
```
POST /api/reports/export-html-with-data
Content-Type: application/json
```
**Request Body:**
```json
{
  "jrxmlFileName": "sales-report.jrxml",
  "parameters": {
    "ReportTitle": "Sales Report",
    "CompanyName": "Rajida Holdings"
  },
  "dataRecords": [
    {
      "productName": "Laptop",
      "quantity": 5,
      "price": 1500.00,
      "total": 7500.00
    }
  ],
  "exportFormat": "html"
}
```

**Response:**
- HTML content with populated data

**Example with cURL:**
```bash
curl -X POST http://localhost:8080/api/reports/export-html-with-data \
  -H "Content-Type: application/json" \
  -d '{
    "jrxmlFileName": "sales-report.jrxml",
    "parameters": {"ReportTitle": "My Report"},
    "dataRecords": [{"productName": "Laptop", "quantity": 5}]
  }' \
  --output report.html
```

### 7. Delete JRXML File
```
DELETE /api/reports/delete/{fileName}
```
**Request Parameter:**
- `fileName`: The name of the file to delete

**Response:**
```json
{
  "success": true,
  "message": "File deleted successfully",
  "data": null
}
```

**Example with cURL:**
```bash
curl -X DELETE http://localhost:8080/api/reports/delete/1702518000000_report.jrxml
```

---

## 📊 Data-Driven Reports ⭐ NEW FEATURE

The service now supports **sending data to populate JRXML templates**!

### What You Can Do:
- Send report data (records/rows) to populate templates
- Combine parameters with data for dynamic reports
- Generate reports with list/table data automatically

### Example Use Cases:
- **Sales Reports**: Send product sales records
- **Invoices**: Send line items and invoice details
- **Employee Directory**: Send employee records
- **Student Transcripts**: Send grade records

### Quick Example:
```bash
curl -X POST http://localhost:8080/api/reports/export-pdf-with-data \
  -H "Content-Type: application/json" \
  -d '{
    "jrxmlFileName": "sales-report.jrxml",
    "parameters": {"ReportTitle": "Q4 Sales"},
    "dataRecords": [
      {"productName": "Laptop", "quantity": 5, "price": 1500},
      {"productName": "Monitor", "quantity": 10, "price": 300}
    ]
  }' \
  --output sales-report.pdf
```

**📖 See [DATA_DRIVEN_REPORTS.md](DATA_DRIVEN_REPORTS.md) for complete documentation and examples!**

---

## Configuration

Edit `src/main/resources/application.yml` to customize:

```yaml
spring:
  application:
    name: rajida-report-service
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

server:
  port: 8080
  servlet:
    context-path: /

upload:
  dir: uploads
```
```
GET /api/reports/list
```
**Response:**
```json
{
  "success": true,
  "message": "Files retrieved successfully",
  "data": [
    "1702518000000_report.jrxml",
    "1702518001000_another_report.jrxml"
  ]
}
```

**Example with cURL:**
```bash
curl http://localhost:8080/api/reports/list
```

### 6. Delete JRXML File
```
DELETE /api/reports/delete/{fileName}
```
**Request Parameter:**
- `fileName`: The name of the file to delete

**Response:**
```json
{
  "success": true,
  "message": "File deleted successfully",
  "data": null
}
```

**Example with cURL:**
```bash
curl -X DELETE http://localhost:8080/api/reports/delete/1702518000000_report.jrxml
```

## Configuration

Edit `src/main/resources/application.yml` to customize:

```yaml
spring:
  application:
    name: rajida-report-service
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

server:
  port: 8080
  servlet:
    context-path: /

upload:
  dir: uploads
```

## Project Structure

```
rajida_report_service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/rajida/report/
│   │   │       ├── RajidaReportApplication.java
│   │   │       ├── controller/
│   │   │       │   └── ReportController.java
│   │   │       ├── service/
│   │   │       │   └── ReportService.java
│   │   │       └── model/
│   │   │           ├── ReportRequest.java
│   │   │           └── ApiResponse.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── uploads/
├── Dockerfile
├── pom.xml
└── README.md
```

## Dependencies

- **Spring Boot 3.2.4**: Web framework
- **JasperReports 6.21.3**: Report generation
- **iText 2.1.7**: PDF rendering
- **Jackson**: JSON serialization

## Error Handling

The API returns appropriate HTTP status codes:

- `200 OK`: Successful request
- `400 Bad Request`: Invalid file or parameters
- `404 Not Found`: File not found
- `500 Internal Server Error`: Server-side error

All error responses include:
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

## Docker Deployment

The included `Dockerfile` uses a multi-stage build:
1. **Builder Stage**: Compiles the Maven project
2. **Runtime Stage**: Runs the application with JRE only

### Build Image
```bash
docker build -t rajida-report-service:1.0.0 .
```

### Run Container
```bash
docker run -d \
  --name rajida-report \
  -p 8080:8080 \
  -v $(pwd)/uploads:/app/uploads \
  rajida-report-service:1.0.0
```

## Testing with Postman

1. **Health Check**
   - Method: `GET`
   - URL: `http://localhost:8080/api/reports/health`

2. **Upload File**
   - Method: `POST`
   - URL: `http://localhost:8080/api/reports/upload`
   - Body: Form-data with `file` parameter

3. **Export PDF**
   - Method: `POST`
   - URL: `http://localhost:8080/api/reports/export-pdf`
   - Body (JSON): `{"jrxmlFileName": "your_file.jrxml", "parameters": {}}`

4. **Export HTML**
   - Method: `POST`
   - URL: `http://localhost:8080/api/reports/export-html`
   - Body (JSON): `{"jrxmlFileName": "your_file.jrxml", "parameters": {}}`

## Future Enhancements

- Database integration for report metadata
- User authentication and authorization
- Report scheduling
- XLSX export format
- Report template management
- Parameter validation
- Caching mechanism

## Troubleshooting

### Issue: "File not found" error
- Ensure the JRXML file was uploaded successfully
- Check the exact filename returned from the upload endpoint

### Issue: "Field not found" in data-driven reports
- Verify that field names in dataRecords exactly match JRXML field names
- Field names are case-sensitive
- Check JRXML template for correct field references using `$F{fieldName}`

### Issue: Report shows no data
- Verify dataRecords array is not empty
- Check that field names match JRXML template exactly
- Ensure data is sent to the correct endpoint (`export-pdf-with-data` or `export-html-with-data`)

### Issue: "No suitable driver" for database
- Add database connection details to your JRXML file
- Update the JRXML with proper data source configuration

### Issue: Font rendering issues in PDF
- The iText library is included for better font rendering
- Ensure fonts are properly embedded in your JRXML

## License

This project is part of Rajida Holdings

## Support

For issues and questions, please contact the development team.
