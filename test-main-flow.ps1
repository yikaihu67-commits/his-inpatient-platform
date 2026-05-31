$ErrorActionPreference = "Stop"

chcp 65001 | Out-Null
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$baseUrl = "http://localhost:8080/api"

function Write-Step {
  param([string]$Message)
  Write-Host ""
  Write-Host ("== {0}" -f $Message) -ForegroundColor Cyan
}

function Invoke-Api {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Path,
    [object]$Body = $null
  )

  $uri = "{0}{1}" -f $baseUrl, $Path
  $headers = @{ "X-Operator-Name" = "main-flow-test" }

  try {
    if ($null -eq $Body) {
      $response = Invoke-WebRequest -Uri $uri -Method $Method -Headers $headers -UseBasicParsing -TimeoutSec 30
    } else {
      $json = $Body | ConvertTo-Json -Depth 20
      $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
      $response = Invoke-WebRequest -Uri $uri -Method $Method -Headers $headers -ContentType "application/json; charset=utf-8" -Body $bytes -UseBasicParsing -TimeoutSec 30
    }

    $payload = $response.Content | ConvertFrom-Json
    if ($null -ne $payload.success -and -not $payload.success) {
      throw ("API returned success=false: {0}" -f $payload.message)
    }
    return $payload.data
  } catch {
    $message = $_.Exception.Message
    if ($_.Exception.Response) {
      try {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream(), [System.Text.Encoding]::UTF8)
        $errorBody = $reader.ReadToEnd()
        if ($errorBody) {
          $errorPayload = $errorBody | ConvertFrom-Json
          if ($errorPayload.message) {
            $message = $errorPayload.message
          } else {
            $message = $errorBody
          }
        }
      } catch {
        $message = $_.Exception.Message
      }
    }
    throw ("{0} {1} failed: {2}" -f $Method, $Path, $message)
  }
}

function Assert-True {
  param([bool]$Condition, [string]$Message)
  if (-not $Condition) {
    throw $Message
  }
}

try {
  Write-Step "Check backend health"
  $health = Invoke-WebRequest -Uri "$baseUrl/health" -UseBasicParsing -TimeoutSec 10
  Write-Host $health.Content

  Write-Step "Check dashboard summary"
  $dashboard = Invoke-Api -Method "GET" -Path "/dashboard/summary"
  Assert-True ($null -ne $dashboard.metrics.currentInHospitalPatientCount) "Dashboard summary should return inpatient metrics."

  $suffix = Get-Date -Format "yyyyMMddHHmmssfff"
  $short = $suffix.Substring($suffix.Length - 6)
  $phone = "138{0}" -f $suffix.Substring($suffix.Length - 8)

  Write-Step "1. Create patient"
  $patient = Invoke-Api -Method "POST" -Path "/patients" -Body @{
    patientNo = "PT$suffix"
    name = "MainFlowPatient$suffix"
    gender = "MALE"
    idCard = "320102199001$short"
    phone = $phone
    birthDate = "1990-01-01"
    address = "Nanjing Xuanwu"
  }
  Write-Host ("patientId={0}, patientNo={1}" -f $patient.id, $patient.patientNo)

  Write-Step "2. Create admission"
  $admission = Invoke-Api -Method "POST" -Path "/admissions" -Body @{
    admissionNo = "ZY$suffix"
    patientId = $patient.id
    departmentName = "Inpatient Dept"
    wardName = "Ward A"
    admissionTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    admissionDiagnosis = "Infection"
    chargeType = "SELF"
    nursingLevel = "LEVEL_2"
    doctorName = "Dr Wang"
    status = "REGISTERED"
  }
  Write-Host ("admissionId={0}, status={1}" -f $admission.id, $admission.status)

  Write-Step "3. Admit patient"
  $admission = Invoke-Api -Method "POST" -Path "/admissions/$($admission.id)/admit"
  Assert-True ($admission.status -eq "IN_HOSPITAL") "Admission status should be IN_HOSPITAL after admit."

  Write-Step "4. Create bed"
  $bed = Invoke-Api -Method "POST" -Path "/beds" -Body @{
    bedNo = "B$suffix"
    wardName = "Ward A"
    roomNo = "101"
    bedType = "NORMAL"
    status = "AVAILABLE"
  }
  Write-Host ("bedId={0}, status={1}" -f $bed.id, $bed.status)

  Write-Step "5. Assign bed"
  $bed = Invoke-Api -Method "POST" -Path "/beds/$($bed.id)/assign" -Body @{ admissionId = $admission.id }
  Assert-True ($bed.status -eq "OCCUPIED") "Bed status should be OCCUPIED after assign."
  Assert-True ($bed.currentAdmissionId -eq $admission.id) "Bed currentAdmissionId should match admissionId."

  Write-Step "6. Create DRUG order"
  $order = Invoke-Api -Method "POST" -Path "/orders" -Body @{
    orderNo = "YZ$suffix"
    admissionId = $admission.id
    patientId = $patient.id
    orderType = "LONG_TERM"
    orderCategory = "DRUG"
    itemName = "Amoxicillin Capsule"
    dosage = "0.5"
    dosageUnit = "g"
    frequency = "tid"
    route = "oral"
    startTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    doctorName = "Dr Wang"
    status = "DRAFT"
    remark = "main flow"
  }

  Write-Step "7. Submit order"
  $order = Invoke-Api -Method "POST" -Path "/orders/$($order.id)/submit"
  Assert-True ($order.status -eq "SUBMITTED") "Order status should be SUBMITTED."

  Write-Step "8. Check order"
  $order = Invoke-Api -Method "POST" -Path "/orders/$($order.id)/check" -Body @{ nurseName = "Nurse Zhao"; remark = "checked" }
  Assert-True ($order.status -eq "CHECKED") "Order status should be CHECKED."

  Write-Step "9. Create nursing execution"
  $execution = Invoke-Api -Method "POST" -Path "/nursing/executions" -Body @{
    orderId = $order.id
    scheduledTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    nurseName = "Nurse Zhao"
    remark = "main flow execution"
  }

  Write-Step "10. Execute nursing record"
  $execution = Invoke-Api -Method "POST" -Path "/nursing/executions/$($execution.id)/execute" -Body @{
    nurseName = "Nurse Zhao"
    result = "done"
    remark = "ok"
  }
  Assert-True ($execution.status -eq "EXECUTED") "Nursing execution status should be EXECUTED."

  Write-Step "11. Create drug"
  $drug = Invoke-Api -Method "POST" -Path "/drugs" -Body @{
    drugCode = "DRUG$suffix"
    drugName = "Amoxicillin Capsule"
    specification = "0.25g*24"
    unit = "box"
    price = 15.50
    stockQuantity = 100
    stockLowerLimit = 10
    status = "ENABLED"
  }

  Write-Step "12. Create checked pharmacy order"
  $pharmacyOrder = Invoke-Api -Method "POST" -Path "/orders" -Body @{
    orderNo = "FYORDER$suffix"
    admissionId = $admission.id
    patientId = $patient.id
    orderType = "LONG_TERM"
    orderCategory = "DRUG"
    itemName = "Amoxicillin Capsule"
    dosage = "0.5"
    dosageUnit = "g"
    frequency = "tid"
    route = "oral"
    startTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    doctorName = "Dr Wang"
    status = "DRAFT"
    remark = "main flow pharmacy order"
  }
  $pharmacyOrder = Invoke-Api -Method "POST" -Path "/orders/$($pharmacyOrder.id)/submit"
  $pharmacyOrder = Invoke-Api -Method "POST" -Path "/orders/$($pharmacyOrder.id)/check" -Body @{ nurseName = "Nurse Zhao"; remark = "checked for pharmacy" }
  Assert-True ($pharmacyOrder.status -eq "CHECKED") "Pharmacy order status should be CHECKED."

  Write-Step "13. Create pharmacy dispense"
  $dispense = Invoke-Api -Method "POST" -Path "/pharmacy/dispenses" -Body @{
    orderId = $pharmacyOrder.id
    drugId = $drug.id
    quantity = 2
    pharmacistName = "Pharmacist Qian"
    remark = "main flow dispense"
  }

  Write-Step "14. Dispense and generate fee"
  $dispense = Invoke-Api -Method "POST" -Path "/pharmacy/dispenses/$($dispense.id)/dispense" -Body @{
    pharmacistName = "Pharmacist Qian"
    remark = "dispensed"
  }
  Assert-True ($dispense.status -eq "DISPENSED") "Dispense status should be DISPENSED."

  Write-Step "14a. Create and bill surgery"
  $surgery = Invoke-Api -Method "POST" -Path "/surgeries" -Body @{
    surgeryNo = "SS$suffix"
    admissionId = $admission.id
    patientId = $patient.id
    surgeryName = "Appendectomy"
    preoperativeDiagnosis = "Appendicitis"
    surgeryLevel = "II"
    surgeryType = "ELECTIVE"
    primaryDoctorName = "Dr Wang"
    surgeryFee = 400.00
    remark = "main flow surgery"
  }
  Assert-True ($surgery.status -eq "APPLIED") "Surgery status should be APPLIED."
  $surgery = Invoke-Api -Method "POST" -Path "/surgeries/$($surgery.id)/schedule" -Body @{
    plannedTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    operatingRoom = "OR-MAIN"
    primaryDoctorName = "Dr Wang"
    anesthesiaMethod = "General"
    anesthesiologistName = "Dr An"
    surgeryFee = 400.00
  }
  Assert-True ($surgery.status -eq "SCHEDULED") "Surgery status should be SCHEDULED."
  $surgery = Invoke-Api -Method "POST" -Path "/surgeries/$($surgery.id)/start" -Body @{
    actualStartTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
  }
  Assert-True ($surgery.status -eq "IN_PROGRESS") "Surgery status should be IN_PROGRESS."
  $surgery = Invoke-Api -Method "POST" -Path "/surgeries/$($surgery.id)/complete" -Body @{
    actualEndTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    surgeryFee = 400.00
    remark = "completed"
  }
  Assert-True ($surgery.status -eq "COMPLETED") "Surgery status should be COMPLETED."
  $surgery = Invoke-Api -Method "POST" -Path "/surgeries/$($surgery.id)/bill" -Body @{
    surgeryFee = 400.00
    remark = "billed"
  }
  Assert-True ($surgery.status -eq "BILLED") "Surgery status should be BILLED."
  $surgeryFees = Invoke-Api -Method "GET" -Path "/fees?page=1&pageSize=20&itemCategory=SURGERY&admissionId=$($admission.id)"
  Assert-True ($surgeryFees.total -ge 1) "Surgery billing should generate a SURGERY fee."

  Write-Step "14b. Create, submit, review and archive medical record"
  $medicalRecord = Invoke-Api -Method "POST" -Path "/medical-records" -Body @{
    recordNo = "BL$suffix"
    admissionId = $admission.id
    patientId = $patient.id
    recordType = "DAILY_COURSE_RECORD"
    title = "Main flow daily record"
    content = "Patient is stable. Continue treatment."
    doctorName = "Dr Wang"
    recordTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    status = "DRAFT"
    remark = "main flow medical record"
  }
  Assert-True ($medicalRecord.status -eq "DRAFT") "Medical record should be DRAFT."
  $medicalRecord = Invoke-Api -Method "POST" -Path "/medical-records/$($medicalRecord.id)/submit" -Body @{ remark = "submitted" }
  Assert-True ($medicalRecord.status -eq "SUBMITTED") "Medical record should be SUBMITTED."
  $medicalRecord = Invoke-Api -Method "POST" -Path "/medical-records/$($medicalRecord.id)/review" -Body @{ remark = "reviewed" }
  Assert-True ($medicalRecord.status -eq "REVIEWED") "Medical record should be REVIEWED."
  $medicalRecord = Invoke-Api -Method "POST" -Path "/medical-records/$($medicalRecord.id)/archive" -Body @{ remark = "archived" }
  Assert-True ($medicalRecord.status -eq "ARCHIVED") "Medical record should be ARCHIVED."

  Write-Step "14c. Create, execute and bill nursing record"
  $nursingRecord = Invoke-Api -Method "POST" -Path "/nursing/records" -Body @{
    recordNo = "HL$suffix"
    orderId = $order.id
    admissionId = $admission.id
    patientId = $patient.id
    nursingType = "DAILY_CARE"
    nursingContent = "Daily inpatient nursing"
    temperature = 36.8
    pulse = 78
    respiration = 18
    bloodPressure = "120/80"
    nursingLevel = "LEVEL_2"
    nurseName = "Nurse Zhao"
    recordTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    status = "RECORDED"
    billable = $true
    nursingFee = 25.00
    remark = "main flow nursing record"
  }
  $nursingRecord = Invoke-Api -Method "POST" -Path "/nursing/records/$($nursingRecord.id)/execute" -Body @{ nurseName = "Nurse Zhao"; remark = "executed" }
  Assert-True ($nursingRecord.status -eq "EXECUTED") "Nursing record should be EXECUTED."
  $nursingRecord = Invoke-Api -Method "POST" -Path "/nursing/records/$($nursingRecord.id)/bill" -Body @{ remark = "billed" }
  Assert-True ($nursingRecord.status -eq "BILLED") "Nursing record should be BILLED."

  Write-Step "14d. Create, report and bill exam/lab request"
  $examLab = Invoke-Api -Method "POST" -Path "/exam-lab-requests" -Body @{
    requestNo = "JY$suffix"
    admissionId = $admission.id
    patientId = $patient.id
    requestType = "EXAM"
    itemCode = "EXAM-MAIN"
    itemName = "Chest CT"
    requestContent = "Main flow exam"
    itemCategory = "IMAGING"
    doctorName = "Dr Wang"
    requestTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    executionDepartment = "Imaging"
    unitPrice = 120.00
    quantity = 1
    status = "REQUESTED"
    remark = "main flow exam"
  }
  Assert-True ($examLab.status -eq "REQUESTED") "Exam/lab request should be REQUESTED."
  $examLab = Invoke-Api -Method "POST" -Path "/exam-lab-requests/$($examLab.id)/schedule" -Body @{
    scheduledTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    executionDepartment = "Imaging"
    executorName = "Tech Sun"
  }
  Assert-True ($examLab.status -eq "SCHEDULED") "Exam/lab request should be SCHEDULED."
  $examLab = Invoke-Api -Method "POST" -Path "/exam-lab-requests/$($examLab.id)/complete" -Body @{
    executedTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    executorName = "Tech Sun"
  }
  Assert-True ($examLab.status -eq "COMPLETED") "Exam/lab request should be COMPLETED."
  $examLab = Invoke-Api -Method "POST" -Path "/exam-lab-requests/$($examLab.id)/report" -Body @{
    reportDoctorName = "Dr Report"
    reportTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    resultSummary = "No obvious abnormality"
    resultDetail = "Main flow report detail"
    abnormalFlag = $false
  }
  Assert-True ($examLab.status -eq "REPORTED") "Exam/lab request should be REPORTED."
  $examLab = Invoke-Api -Method "POST" -Path "/exam-lab-requests/$($examLab.id)/bill" -Body @{ unitPrice = 120.00; quantity = 1; remark = "billed" }
  Assert-True ($examLab.status -eq "BILLED") "Exam/lab request should be BILLED."

  Write-Step "15. Pay deposit"
  $deposit = Invoke-Api -Method "POST" -Path "/deposits/pay" -Body @{
    admissionId = $admission.id
    patientId = $patient.id
    amount = 500.00
    paymentMethod = "CASH"
    operatorName = "Cashier"
    remark = "main flow deposit"
  }
  Assert-True ($deposit.status -eq "SUCCESS") "Deposit status should be SUCCESS."

  Write-Step "16. Query inpatient account summary"
  $account = Invoke-Api -Method "GET" -Path "/inpatient-accounts/$($admission.id)/summary"
  Assert-True ([decimal]$account.totalFeeAmount -gt 0) "Dispense should generate fee."
  Write-Host ("totalFee={0}, depositBalance={1}, unpaid={2}" -f $account.totalFeeAmount, $account.depositBalance, $account.unpaidAmount)

  Write-Step "16a. Verify patient self-service kiosk queries"
  $selfService = Invoke-Api -Method "POST" -Path "/patient-self-service/verify" -Body @{
    admissionId = $admission.id
    phone = $phone
    idCardLast4 = $short.Substring($short.Length - 4)
    patientName = $patient.name
  }
  Assert-True ($selfService.admissionId -eq $admission.id) "Self-service verify should return the current admission."
  $selfBill = Invoke-Api -Method "GET" -Path "/patient-self-service/bill-summary?patientId=$($patient.id)&admissionId=$($admission.id)"
  Assert-True ([decimal]$selfBill.totalAmount -eq [decimal]$account.totalFeeAmount) "Self-service bill total should match account summary."
  $selfExamLabs = Invoke-Api -Method "GET" -Path "/patient-self-service/exam-labs?patientId=$($patient.id)&admissionId=$($admission.id)"
  Assert-True (@($selfExamLabs).Count -ge 1) "Self-service should return exam/lab records."
  $selfSurgeries = Invoke-Api -Method "GET" -Path "/patient-self-service/surgeries?patientId=$($patient.id)&admissionId=$($admission.id)"
  Assert-True (@($selfSurgeries).Count -ge 1) "Self-service should return surgery records."

  Write-Step "16b. Verify patient mobile appointment service"
  $mobileLogin = Invoke-Api -Method "POST" -Path "/patient-mobile/login" -Body @{
    admissionId = $admission.id
    phone = $phone
    idCardLast4 = $short.Substring($short.Length - 4)
  }
  Assert-True ($mobileLogin.patient.admissionId -eq $admission.id) "Patient mobile login should bind the current admission."
  $demoMobileLogin = Invoke-Api -Method "POST" -Path "/patient-mobile/login" -Body @{
    admissionNo = "ZY-DEMO-002"
    phone = "13900000002"
    idCardLast4 = "0002"
  }
  Assert-True ($demoMobileLogin.patient.admissionNo -eq "ZY-DEMO-002") "Patient miniapp demo login should support admissionNo."
  $mobileHome = Invoke-Api -Method "GET" -Path "/patient-mobile/home?patientId=$($patient.id)&admissionId=$($admission.id)"
  Assert-True ([decimal]$mobileHome.billSummary.totalAmount -eq [decimal]$account.totalFeeAmount) "Patient mobile bill total should match account summary."
  $mobileAppointment = Invoke-Api -Method "POST" -Path "/patient-mobile/appointments" -Body @{
    patientId = $patient.id
    admissionId = $admission.id
    appointmentType = "EXAM_APPOINTMENT"
    appointmentItem = "Main flow mobile appointment"
    appointmentDate = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
    timeSlot = "AM"
    contactPhone = $phone
    remark = "main flow mobile appointment"
  }
  Assert-True ($mobileAppointment.status -eq "REQUESTED") "Mobile appointment should be REQUESTED."
  $mobileAppointment = Invoke-Api -Method "POST" -Path "/patient-mobile/appointments/cancel?appointmentId=$($mobileAppointment.id)" -Body @{ remark = "cancel smoke" }
  Assert-True ($mobileAppointment.status -eq "CANCELLED") "Mobile appointment should be cancellable before completion."

  Write-Step "17. Create discharge settlement"
  $discharge = Invoke-Api -Method "POST" -Path "/discharges/create" -Body @{
    dischargeNo = "CY$suffix"
    admissionId = $admission.id
    patientId = $patient.id
    dischargeTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    operatorName = "Cashier"
    remark = "main flow discharge"
  }

  Write-Step "18. Settle discharge"
  $discharge = Invoke-Api -Method "POST" -Path "/discharges/$($discharge.id)/settle" -Body @{
    actualPayment = $discharge.unpaidAmount
    paymentMethod = "CASH"
    operatorName = "Cashier"
    dischargeTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    remark = "settled"
  }
  Assert-True ($discharge.status -eq "SETTLED") "Discharge status should be SETTLED."

  Write-Step "19. Verify discharge closure"
  $admissionAfter = Invoke-Api -Method "GET" -Path "/admissions/$($admission.id)"
  $bedAfter = Invoke-Api -Method "GET" -Path "/beds/$($bed.id)"
  Assert-True ($admissionAfter.status -eq "DISCHARGED") "Admission status should be DISCHARGED after discharge settlement."
  Assert-True ($bedAfter.status -eq "AVAILABLE") "Bed status should be AVAILABLE after discharge settlement."
  Assert-True ($null -eq $bedAfter.currentAdmissionId) "Bed currentAdmissionId should be null after discharge settlement."
  Assert-True ([decimal]$discharge.totalFeeAmount -eq [decimal]$account.totalFeeAmount) "Discharge total fee should match account summary before settlement."
  $selfDischarge = Invoke-Api -Method "GET" -Path "/patient-self-service/discharge?patientId=$($patient.id)&admissionId=$($admission.id)"
  Assert-True ($selfDischarge.status -eq "SETTLED") "Self-service discharge status should be SETTLED after settlement."
  Write-Host ("admission={0}, bed={1}, currentAdmissionId={2}" -f $admissionAfter.status, $bedAfter.status, $bedAfter.currentAdmissionId)

  Write-Step "20. Verify operation logs"
  Start-Sleep -Seconds 1
  $logs = Invoke-Api -Method "GET" -Path "/operation-logs?page=1&pageSize=50"
  Assert-True ($logs.total -ge 10) "Main flow write operations should generate operation logs."
  Write-Host ("operationLogTotal={0}" -f $logs.total)

  Write-Host ""
  Write-Host "Main flow regression test passed." -ForegroundColor Green
} catch {
  Write-Host ""
  Write-Host ("Main flow regression test failed: {0}" -f $_.Exception.Message) -ForegroundColor Red
  exit 1
}
