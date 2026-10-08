# Smart PG & Hostel Management System - Verification Test Suite
$base = "http://localhost:2007"

function Print-Result($name, $status, $detail) {
    if ($status -eq $true) {
        Write-Host "✅ [PASS] $name : $detail" -ForegroundColor Green
    } else {
        Write-Host "❌ [FAIL] $name : $detail" -ForegroundColor Red
    }
}

Write-Host "================================================="
Write-Host "1. Testing Login Page Cleanliness"
Write-Host "================================================="
try {
    $loginResp = Invoke-WebRequest -Uri "$base/login" -SessionVariable globalSession
    $content = $loginResp.Content
    $hasRoleBtns = ($content -match "Warden login") -or ($content -match "Student login") -or ($content -match "admin123") -or ($content -match "password123")
    Print-Result "Login Page Clean" (-not $hasRoleBtns) "No role selection buttons or demo passwords found."
} catch {
    Print-Result "Login Page Clean" $false $_.Exception.Message
}

Write-Host "`n================================================="
Write-Host "2. Testing Role Authentication & Automatic Redirects"
Write-Host "================================================="
$roles = @(
    @{ Role="WARDEN"; User="warden@smarthostel.com"; Pass="Warden@123"; Target="/warden/dashboard" },
    @{ Role="STUDENT"; User="student@smarthostel.com"; Pass="Student@123"; Target="/student/dashboard" },
    @{ Role="ACCOUNTANT"; User="accountant@smarthostel.com"; Pass="Accountant@123"; Target="/accountant/dashboard" },
    @{ Role="COMPLAINT_STAFF"; User="complaint@smarthostel.com"; Pass="Complaint@123"; Target="/complaint-staff/dashboard" }
)

foreach ($r in $roles) {
    try {
        $sess = New-Object Microsoft.PowerShell.Commands.WebRequestSession
        $postResp = Invoke-WebRequest -Uri "$base/login" -Method POST -Body @{ username=$r.User; password=$r.Pass } -WebSession $sess -MaximumRedirection 0 -ErrorAction SilentlyContinue
        $loc = $postResp.Headers.Location
        if (-not $loc) { $loc = $postResp.Headers["Location"] }
        $match = ($loc -eq $r.Target)
        Print-Result "$($r.Role) Login Redirect" $match "Target: $($r.Target), Got: $loc"
    } catch {
        Print-Result "$($r.Role) Login Redirect" $false $_.Exception.Message
    }
}

Write-Host "`n================================================="
Write-Host "3. Testing Role Authorization & Access Control"
Write-Host "================================================="
# Student trying to access Warden dashboard
try {
    $stuSess = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    Invoke-WebRequest -Uri "$base/login" -Method POST -Body @{ username="student@smarthostel.com"; password="Student@123" } -WebSession $stuSess -MaximumRedirection 0 -ErrorAction SilentlyContinue | Out-Null
    
    $wardenAccess = Invoke-WebRequest -Uri "$base/warden/dashboard" -WebSession $stuSess -MaximumRedirection 0 -ErrorAction SilentlyContinue
    $status = $wardenAccess.StatusCode
    $loc = $wardenAccess.Headers.Location
    $blocked = ($status -eq 403) -or ($loc -match "access-denied") -or ($wardenAccess.Content -match "Access Denied")
    Print-Result "Student Blocked from Warden Dashboard" $blocked "StatusCode: $status, Location: $loc"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Print-Result "Student Blocked from Warden Dashboard" ($statusCode -eq 403) "Caught expected HTTP $statusCode"
}

Write-Host "`n================================================="
Write-Host "4. Testing Dynamic Registration Validation"
Write-Host "================================================="
# Test registration of a new student with full room
try {
    $regSess = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    # Room A-101 has capacity 2, current student STU-2026-001 has bed 1
    # Let's register student 2 for bed 2
    $regBody = @{
        role = "STUDENT"
        name = "Kavita Rao"
        email = "kavita.rao@test.com"
        phone = "9876543299"
        password = "Password@123"
        confirmPassword = "Password@123"
        gender = "FEMALE"
        admissionNumber = "STU-TEST-002"
        college = "Apex Engineering"
        course = "B.Tech IT"
        yearOfStudy = "2"
        guardianName = "Suresh Rao"
        guardianPhone = "9876500099"
        emergencyContact = "9876500099"
        address = "123 Test Street"
        hostelName = "Block A"
        roomNumber = "A-101"
        bedNumber = "2"
    }
    $regResp = Invoke-WebRequest -Uri "$base/register" -Method POST -Body $regBody -WebSession $regSess -MaximumRedirection 0 -ErrorAction SilentlyContinue
    $loc = $regResp.Headers.Location
    Print-Result "Student Bed Allocation Registration" ($loc -match "/login\?registered") "Successfully allocated bed: $loc"

    # Now room A-101 is full (capacity 2, beds 1 and 2 occupied). Trying to register bed 3 or into full room:
    $fullBody = @{
        role = "STUDENT"
        name = "Rohit Verma"
        email = "rohit.verma@test.com"
        phone = "9876543298"
        password = "Password@123"
        confirmPassword = "Password@123"
        gender = "MALE"
        admissionNumber = "STU-TEST-003"
        college = "Apex Engineering"
        course = "B.Tech IT"
        yearOfStudy = "2"
        guardianName = "Mahesh Verma"
        guardianPhone = "9876500098"
        emergencyContact = "9876500098"
        address = "456 Test Ave"
        hostelName = "Block A"
        roomNumber = "A-101"
        bedNumber = "1"
    }
    $fullResp = Invoke-WebRequest -Uri "$base/register" -Method POST -Body $fullBody -WebSession $regSess -MaximumRedirection 0 -ErrorAction SilentlyContinue
    $isRejected = ($fullResp.Content -match "no available bed" -or $fullResp.Content -match "already occupied" -or $fullResp.Content -match "capacity is full")
    Print-Result "Full Room / Bed Occupied Validation" $isRejected "Validation message triggered: Room/Bed rejected as expected."
} catch {
    Print-Result "Registration Tests" $false $_.Exception.Message
}

Write-Host "`n================================================="
Write-Host "5. Testing Leave Request Workflow"
Write-Host "================================================="
try {
    # Student logs in
    $stuSess = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    Invoke-WebRequest -Uri "$base/login" -Method POST -Body @{ username="student@smarthostel.com"; password="Student@123" } -WebSession $stuSess -MaximumRedirection 0 -ErrorAction SilentlyContinue | Out-Null
    
    # Student submits leave request
    $leaveBody = @{
        startDate = "2026-10-15"
        endDate = "2026-10-18"
        reason = "Attending family function in hometown"
        emergencyContact = "9876500001"
    }
    $leavePost = Invoke-WebRequest -Uri "$base/student/leaves" -Method POST -Body $leaveBody -WebSession $stuSess -MaximumRedirection 0 -ErrorAction SilentlyContinue
    Print-Result "Student Submits Leave Request" ($leavePost.Headers.Location -match "success") "Location: $($leavePost.Headers.Location)"

    # Warden logs in
    $wrdSess = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    Invoke-WebRequest -Uri "$base/login" -Method POST -Body @{ username="warden@smarthostel.com"; password="Warden@123" } -WebSession $wrdSess -MaximumRedirection 0 -ErrorAction SilentlyContinue | Out-Null
    
    # Warden views leaves and approves the pending leave
    $wrdLeaves = Invoke-WebRequest -Uri "$base/warden/leaves" -WebSession $wrdSess
    # Extract leave ID from the form action
    if ($wrdLeaves.Content -match '/warden/leaves/(\d+)/approve') {
        $leaveId = $matches[1]
        $apprResp = Invoke-WebRequest -Uri "$base/warden/leaves/$leaveId/approve" -Method POST -Body @{ remarks="Approved by Warden. Safe journey." } -WebSession $wrdSess -MaximumRedirection 0 -ErrorAction SilentlyContinue
        Print-Result "Warden Approves Leave Request" ($apprResp.Headers.Location -match "success") "Leave ID $leaveId approved by Warden"
    } else {
        Print-Result "Warden Leaves Page" $false "No pending leave button found"
    }
} catch {
    Print-Result "Leave Request Workflow" $false $_.Exception.Message
}

Write-Host "`n================================================="
Write-Host "6. Testing Visitor Request Workflow"
Write-Host "================================================="
try {
    # Student submits visitor request
    $visBody = @{
        visitorName = "Sunil Kumar"
        relation = "Uncle"
        visitorPhone = "9811223344"
        visitDate = "2026-10-12"
        visitTime = "15:00"
        purpose = "Delivering college textbooks and essentials"
    }
    $visPost = Invoke-WebRequest -Uri "$base/student/visitors" -Method POST -Body $visBody -WebSession $stuSess -MaximumRedirection 0 -ErrorAction SilentlyContinue
    Print-Result "Student Submits Visitor Request" ($visPost.Headers.Location -match "success") "Location: $($visPost.Headers.Location)"

    # Warden views visitors and approves
    $wrdVisitors = Invoke-WebRequest -Uri "$base/warden/visitors" -WebSession $wrdSess
    if ($wrdVisitors.Content -match '/warden/visitors/(\d+)/approve') {
        $visId = $matches[1]
        $visAppr = Invoke-WebRequest -Uri "$base/warden/visitors/$visId/approve" -Method POST -Body @{ remarks="Visitor entry approved." } -WebSession $wrdSess -MaximumRedirection 0 -ErrorAction SilentlyContinue
        Print-Result "Warden Approves Visitor Request" ($visAppr.Headers.Location -match "success") "Visitor ID $visId approved"
    } else {
        Print-Result "Warden Visitors Page" $false "No pending visitor button found"
    }
} catch {
    Print-Result "Visitor Request Workflow" $false $_.Exception.Message
}

Write-Host "`n================================================="
Write-Host "7. Testing Complete Complaint Workflow"
Write-Host "================================================="
try {
    # 7.1 Student creates complaint
    $cmpBody = @{
        category = "ELECTRICAL"
        title = "Ceiling fan speed regulator broken"
        description = "The fan in room A-101 is stuck at high speed and the regulator does not adjust."
        priority = "HIGH"
    }
    $cmpPost = Invoke-WebRequest -Uri "$base/student/complaints" -Method POST -Body $cmpBody -WebSession $stuSess -MaximumRedirection 0 -ErrorAction SilentlyContinue
    Print-Result "Student Submits Complaint" ($cmpPost.Headers.Location -match "success") "Location: $($cmpPost.Headers.Location)"

    # 7.2 Warden reviews and assigns to Electrical staff
    $wrdComplaints = Invoke-WebRequest -Uri "$base/warden/complaints" -WebSession $wrdSess
    if ($wrdComplaints.Content -match 'name="complaintId" value="(\d+)"') {
        $complaintId = $matches[1]
        # Electrical staff ID is from staff table (seeded CMP-001)
        # Find staff id in options
        if ($wrdComplaints.Content -match 'value="(\d+)">Electrical Maintenance Staff') {
            $staffId = $matches[1]
        } else {
            $staffId = "1"
        }
        $assignBody = @{
            department = "ELECTRICAL"
            staffId = $staffId
            priority = "HIGH"
        }
        $assignResp = Invoke-WebRequest -Uri "$base/warden/complaints/$complaintId/assign" -Method POST -Body $assignBody -WebSession $wrdSess -MaximumRedirection 0 -ErrorAction SilentlyContinue
        Print-Result "Warden Assigns Complaint" ($assignResp.Headers.Location -match "success") "Complaint $complaintId assigned to Staff $staffId"

        # 7.3 Complaint Staff logs in, views assigned complaint, accepts (IN_PROGRESS)
        $cmpStaffSess = New-Object Microsoft.PowerShell.Commands.WebRequestSession
        Invoke-WebRequest -Uri "$base/login" -Method POST -Body @{ username="complaint@smarthostel.com"; password="Complaint@123" } -WebSession $cmpStaffSess -MaximumRedirection 0 -ErrorAction SilentlyContinue | Out-Null
        
        $acceptResp = Invoke-WebRequest -Uri "$base/complaint-staff/complaints/$complaintId/status" -Method POST -Body @{ status="IN_PROGRESS"; notes="Work started, inspecting the regulator switchboard." } -WebSession $cmpStaffSess -MaximumRedirection 0 -ErrorAction SilentlyContinue
        Print-Result "Complaint Staff Updates Status to IN_PROGRESS" ($acceptResp.Headers.Location -match "success") "Status set to IN_PROGRESS"

        # 7.4 Complaint Staff resolves complaint
        $resolveResp = Invoke-WebRequest -Uri "$base/complaint-staff/complaints/$complaintId/resolve" -Method POST -Body @{ resolutionRemarks="Replaced the speed regulator switch and verified proper fan operation." } -WebSession $cmpStaffSess -MaximumRedirection 0 -ErrorAction SilentlyContinue
        Print-Result "Complaint Staff Resolves Complaint" ($resolveResp.Headers.Location -match "success") "Complaint $complaintId marked RESOLVED"

        # 7.5 Student verifies and gives rating 5 + feedback -> CLOSED
        $feedbackBody = @{
            rating = "5"
            feedback = "Excellent prompt service! Fan regulator works perfectly now."
        }
        $fbResp = Invoke-WebRequest -Uri "$base/student/complaints/$complaintId/feedback" -Method POST -Body $feedbackBody -WebSession $stuSess -MaximumRedirection 0 -ErrorAction SilentlyContinue
        Print-Result "Student Submits Rating & Feedback" ($fbResp.Headers.Location -match "success") "Feedback recorded, complaint CLOSED"
    } else {
        Print-Result "Warden Complaints Finding" $false "Could not locate newly created complaint ID"
    }
} catch {
    Print-Result "Complaint Workflow" $false $_.Exception.Message
}

Write-Host "`n================================================="
Write-Host "8. Testing Accountant Dashboard & Database Counts"
Write-Host "================================================="
try {
    $accSess = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    Invoke-WebRequest -Uri "$base/login" -Method POST -Body @{ username="accountant@smarthostel.com"; password="Accountant@123" } -WebSession $accSess -MaximumRedirection 0 -ErrorAction SilentlyContinue | Out-Null
    $accDash = Invoke-WebRequest -Uri "$base/accountant/dashboard" -WebSession $accSess
    $hasFakeNumbers = ($accDash.Content -match "2,15,000") -or ($accDash.Content -match "250 students")
    Print-Result "Accountant Dashboard Database-Driven" (-not $hasFakeNumbers) "No hardcoded fake collection or student counts"
} catch {
    Print-Result "Accountant Dashboard" $false $_.Exception.Message
}

Write-Host "`n================================================="
Write-Host "Test Suite Execution Completed!"
Write-Host "================================================="
