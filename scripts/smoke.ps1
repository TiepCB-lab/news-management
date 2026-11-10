#Requires -Version 7.0
param(
    [string]$BaseUrl = 'http://localhost:8080',
    [string]$AdminUsername = 'admin',
    [string]$AdminPassword = 'admin123'
)

$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')
$marker = 'smoke_' + [Guid]::NewGuid().ToString('N').Substring(0, 12)
$imagePath = Join-Path ([IO.Path]::GetTempPath()) "$marker.png"
$newsId = $null
$fileName = $null
$adminHeaders = @{}

function Send-Request {
    param([string]$Method, [string]$Path, [int]$Expected = 200,
        [hashtable]$Headers = @{}, [hashtable]$Body, [hashtable]$Form)
    $options = @{ Method = $Method; Uri = "$BaseUrl$Path"; Headers = $Headers
        SkipHttpErrorCheck = $true; TimeoutSec = 20 }
    if ($Body) {
        $options.Body = $Body | ConvertTo-Json -Compress
        $options.ContentType = 'application/json; charset=utf-8'
    }
    if ($Form) { $options.Form = $Form }
    $response = Invoke-WebRequest @options
    if ($response.StatusCode -ne $Expected) {
        throw "$Method ${Path}: expected $Expected, received $($response.StatusCode)"
    }
    $response
}

function Read-Json($response) { $response.Content | ConvertFrom-Json -Depth 50 }
function Assert-Check([bool]$Passed, [string]$Message) { if (-not $Passed) { throw $Message } }

try {
    $spec = (Send-Request GET '/v3/api-docs').Content | ConvertFrom-Json -AsHashtable -Depth 50
    $ui = Send-Request GET '/swagger-ui/index.html'
    Assert-Check ($ui.Content -match 'Swagger UI') 'Swagger UI HTML is missing'
    $null = Send-Request GET '/v3/api-docs/swagger-config'
    Assert-Check ($spec.components.securitySchemes.bearerAuth.scheme -eq 'bearer') 'Bearer scheme is missing'
    $paths = $spec.paths
    foreach ($endpoint in '/api/v1/auth/register', '/api/v1/admin/news', '/api/v1/files/upload') {
        Assert-Check ($paths[$endpoint].post.responses.Contains('201')) "OpenAPI is missing 201: $endpoint"
    }
    Assert-Check ($paths['/api/v1/admin/news'].post.security[0].Contains('bearerAuth')) 'Private operation lacks Bearer authorization'
    Assert-Check ($paths['/api/v1/public/news'].get.parameters.name -contains 'categoryId') 'Public query parameters are missing'
    $upload = $paths['/api/v1/files/upload'].post.requestBody.content['multipart/form-data'].schema
    if ($upload.'$ref') { $upload = $spec.components.schemas[($upload.'$ref' -split '/')[-1]] }
    Assert-Check ($upload.properties.file.format -eq 'binary') 'Swagger upload input is not a file'

    $seed = Read-Json (Send-Request GET '/api/v1/public/news?limit=2&sortBy=id&sortDir=asc')
    Assert-Check ($seed.data.Count -eq 2 -and $seed.meta.totalPages -gt 1 -and
        $seed.data[0].id -lt $seed.data[1].id) 'Seed data did not demonstrate sorted pages'
    $categoryId = $seed.data[0].categoryId
    $null = Send-Request GET '/api/v1/admin/news' 401

    $admin = Read-Json (Send-Request POST '/api/v1/auth/login' -Body @{
        username = $AdminUsername; password = $AdminPassword
    })
    Assert-Check ($admin.data.role -eq 'ROLE_ADMIN') 'Demo account is not ADMIN'
    $adminHeaders = @{ Authorization = "Bearer $($admin.data.accessToken)" }
    $password = [Guid]::NewGuid().ToString('N')
    $null = Send-Request POST '/api/v1/auth/register' 201 -Body @{
        username = $marker; email = "$marker@example.com"; password = $password
    }
    $user = Read-Json (Send-Request POST '/api/v1/auth/login' -Body @{ username = $marker; password = $password })
    Assert-Check ($user.data.role -eq 'ROLE_USER') 'Registered account is not USER'
    $headers = @{ Authorization = "Bearer $($user.data.accessToken)" }

    $png = [Convert]::FromBase64String('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+j5l8AAAAASUVORK5CYII=')
    [IO.File]::WriteAllBytes($imagePath, $png)
    $file = (Read-Json (Send-Request POST '/api/v1/files/upload' 201 $headers -Form @{
        file = Get-Item -LiteralPath $imagePath
    })).data
    $fileName = $file.fileName
    $download = Send-Request GET $file.fileUrl
    Assert-Check ([Convert]::ToBase64String($download.RawContentStream.ToArray()) -eq
        [Convert]::ToBase64String($png)) 'Downloaded image differs from upload'

    $article = (Read-Json (Send-Request POST '/api/v1/admin/news' 201 $headers -Body @{
        title = "Demo $marker"; content = 'Draft'; categoryId = $categoryId
        isPublished = $false; thumbnailUrl = $file.fileUrl
    })).data
    $newsId = $article.id
    $null = Send-Request GET "/api/v1/public/news/$newsId" 404
    $content = 'Tin tức đã cập nhật'
    $null = Send-Request PUT "/api/v1/admin/news/$newsId" 200 $headers -Body @{
        content = $content; isPublished = $true
    }
    $public = (Read-Json (Send-Request GET "/api/v1/public/news/$($article.slug)")).data
    Assert-Check ($public.id -eq $newsId -and $public.content -eq $content -and
        $public.thumbnailUrl -eq $file.fileUrl -and $public.viewCount -eq 1) 'Public article fields or view count differ'
    $query = "keyword=$marker&categoryId=$categoryId&sortBy=title&sortDir=asc&limit=1"
    foreach ($endpoint in '/api/v1/public/news', '/api/v1/admin/news') {
        $page = Read-Json (Send-Request GET "${endpoint}?$query" 200 $headers)
        Assert-Check ($page.meta.totalElements -eq 1 -and $page.data[0].id -eq $newsId) "Search/filter failed: $endpoint"
    }
    $null = Send-Request DELETE "/api/v1/admin/news/$newsId" 403 $headers
    $null = Send-Request DELETE "/api/v1/files/$fileName" 403 $headers
    $null = Send-Request DELETE "/api/v1/admin/news/$newsId" 200 $adminHeaders
    $null = Send-Request GET "/api/v1/public/news/$newsId" 404
    $newsId = $null
    $null = Send-Request DELETE "/api/v1/files/$fileName" 200 $adminHeaders
    $null = Send-Request GET $file.fileUrl 404
    $fileName = $null
    Write-Output 'PASS: Swagger, register/login, news CRUD/search/sort, roles, upload/download/delete'
} finally {
    if ($newsId) { try { $null = Send-Request DELETE "/api/v1/admin/news/$newsId" 200 $adminHeaders } catch { Write-Warning "Cleanup news $newsId failed" } }
    if ($fileName) { try { $null = Send-Request DELETE "/api/v1/files/$fileName" 200 $adminHeaders } catch { Write-Warning "Cleanup file $fileName failed" } }
    if (Test-Path -LiteralPath $imagePath) { Remove-Item -LiteralPath $imagePath }
}
