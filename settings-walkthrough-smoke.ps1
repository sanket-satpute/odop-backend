$base='http://localhost:50982'
$stamp=[DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$email="settingswalk$stamp@test.com"
$pwd='Cust@12345'
$phone=[int64](8100000000 + ($stamp % 899999999))

$create=@{
  fullName='Settings Walkthrough User'
  emailAddress=$email
  password=$pwd
  contactNumber=$phone
  address='Addr'
  city='Mumbai'
  state='Maharashtra'
  pinCode='400001'
} | ConvertTo-Json

Invoke-RestMethod -Uri "$base/odop/customer/create_account" -Method Post -Body $create -ContentType 'application/json' | Out-Null
$auth=Invoke-RestMethod -Uri "$base/authenticate" -Method Post -Body (@{username=$email;password=$pwd}|ConvertTo-Json) -ContentType 'application/json'
$cid=$auth.user.customerId
$h=@{Authorization="Bearer $($auth.jwt)"}

$result=[ordered]@{}
$result.customerId=$cid
$result.email=$email

try {
  $prefs=Invoke-RestMethod -Uri "$base/odop/customer/$cid/preferences" -Method Get -Headers $h
  $result.loadPreferences='PASS'
} catch { $result.loadPreferences='FAIL' }

try {
  $channels=@{app=$false;email=$true;sms=$true;whatsapp=$true;push=$true}|ConvertTo-Json
  $resp=Invoke-RestMethod -Uri "$base/odop/customer/$cid/preferences/notifications/channels" -Method Put -Headers $h -Body $channels -ContentType 'application/json'
  $result.notificationSave = if($resp.sms -eq $true -and $resp.app -eq $false){'PASS'} else {'FAIL'}
} catch { $result.notificationSave='FAIL' }

try {
  Invoke-RestMethod -Uri "$base/odop/customer/$cid/preferences/security/2fa/enable" -Method Post -Headers $h -Body (@{method='authenticator'}|ConvertTo-Json) -ContentType 'application/json' | Out-Null
  $sec=Invoke-RestMethod -Uri "$base/odop/customer/$cid/preferences/security" -Method Get -Headers $h
  $result.twoFactorEnable = if($sec.twoFactorEnabled -eq $true){'PASS'} else {'FAIL'}
} catch { $result.twoFactorEnable='FAIL' }

try {
  Invoke-RestMethod -Uri "$base/odop/customer/$cid/preferences/security/2fa/disable" -Method Post -Headers $h -Body '{}' -ContentType 'application/json' | Out-Null
  $sec2=Invoke-RestMethod -Uri "$base/odop/customer/$cid/preferences/security" -Method Get -Headers $h
  $result.twoFactorDisable = if($sec2.twoFactorEnabled -eq $false){'PASS'} else {'FAIL'}
} catch { $result.twoFactorDisable='FAIL' }

try {
  $appearance=@{themeMode='dark';fontSize='large';highContrast=$true;accentColor='#FF6B35';compactMode=$false;animations=$true}|ConvertTo-Json
  $a=Invoke-RestMethod -Uri "$base/odop/customer/$cid/preferences/appearance" -Method Put -Headers $h -Body $appearance -ContentType 'application/json'
  $result.appearanceSave = if($a.themeMode -eq 'dark' -and $a.fontSize -eq 'large' -and $a.highContrast -eq $true){'PASS'} else {'FAIL'}
} catch { $result.appearanceSave='FAIL' }

try {
  Invoke-RestMethod -Uri "$base/odop/customer/$cid/preferences/services/google/connect" -Method Post -Headers $h -Body (@{externalId='google-test-id'}|ConvertTo-Json) -ContentType 'application/json' | Out-Null
  $sv=Invoke-RestMethod -Uri "$base/odop/customer/$cid/preferences/services" -Method Get -Headers $h
  $g=$sv | Where-Object { $_.id -eq 'google' } | Select-Object -First 1
  $result.serviceConnect = if($g.connected -eq $true){'PASS'} else {'FAIL'}
} catch { $result.serviceConnect='FAIL' }

try {
  Invoke-RestMethod -Uri "$base/odop/customer/$cid/preferences/services/google/disconnect" -Method Post -Headers $h -Body '{}' -ContentType 'application/json' | Out-Null
  $sv2=Invoke-RestMethod -Uri "$base/odop/customer/$cid/preferences/services" -Method Get -Headers $h
  $g2=$sv2 | Where-Object { $_.id -eq 'google' } | Select-Object -First 1
  $result.serviceDisconnect = if($g2.connected -eq $false){'PASS'} else {'FAIL'}
} catch { $result.serviceDisconnect='FAIL' }

try {
  $sessions=Invoke-RestMethod -Uri "$base/odop/customer/$cid/preferences/security/sessions" -Method Get -Headers $h
  $result.viewActiveSessions = if(@($sessions).Count -ge 1){'PASS'} else {'FAIL'}
} catch { $result.viewActiveSessions='FAIL' }

try {
  $export=Invoke-RestMethod -Uri "$base/odop/customer/$cid/preferences/export" -Method Get -Headers $h
  $result.downloadData = if($null -ne $export.profile -and $null -ne $export.preferences){'PASS'} else {'FAIL'}
} catch { $result.downloadData='FAIL' }

# Destructive action on dedicated disposable account
try {
  $email2="settingsdeact$stamp@test.com"
  $phone2=[int64](8200000000 + ($stamp % 899999999))
  $create2=@{fullName='Settings Deact User';emailAddress=$email2;password=$pwd;contactNumber=$phone2;address='Addr';city='Mumbai';state='Maharashtra';pinCode='400001'}|ConvertTo-Json
  Invoke-RestMethod -Uri "$base/odop/customer/create_account" -Method Post -Body $create2 -ContentType 'application/json' | Out-Null
  $auth2=Invoke-RestMethod -Uri "$base/authenticate" -Method Post -Body (@{username=$email2;password=$pwd}|ConvertTo-Json) -ContentType 'application/json'
  $cid2=$auth2.user.customerId
  $h2=@{Authorization="Bearer $($auth2.jwt)"}
  $deact=Invoke-RestMethod -Uri "$base/odop/customer/$cid2/preferences/deactivate" -Method Post -Headers $h2 -Body '{}' -ContentType 'application/json'
  $blocked=$false
  try { Invoke-RestMethod -Uri "$base/authenticate" -Method Post -Body (@{username=$email2;password=$pwd}|ConvertTo-Json) -ContentType 'application/json' | Out-Null } catch { $blocked=$true }
  $result.deactivateAccount = if($deact.status -eq 'inactive' -and $blocked){'PASS'} else {'FAIL'}
} catch { $result.deactivateAccount='FAIL' }

$result | ConvertTo-Json -Depth 4
