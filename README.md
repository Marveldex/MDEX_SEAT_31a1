# MDEX_SEAT_31a1

This is Sample Application for MDEX_VENUS board & sensor test

It can be operated only in Android environment.

we use BLE 4.0 communication

================================================================

노르딕 nrfUART 소스코드 주소
https://github.com/hubuhubu/Android-nRF-UART

Main UI 
<div align = "center">
<img src="https://github.com/Marveldex/MDEX_SEAR_31a1/blob/master/Image/UI.png" />
</div>


Protocol
<div align = "center">
<img src="https://github.com/Marveldex/MDEX_SEAR_31a1/blob/master/Image/protocol_1.png" />
</div>

Sensor_Number
<div align = "center">
<img src="https://github.com/Marveldex/MDEX_SEAR_31a1/blob/master/Image/seat_numbers.png" />
</div>

<div align = "center">
<img src="https://github.com/Marveldex/MDEX_SEAR_31a1/blob/master/Image/protocol_2.png" />
</div>


History
---------------------------------------
2018-02-23

수정사항

 
- BLE 연결
	- 문제 : BLE연결이 끊어졌을 경우 재연결 시 사용자가 수동으로 연결해야 하는 현상
	- 해결 : 연결이 끊어 졌을 경우 Application 상단에 BLE연결 상태를 Blind로 표시하고 1초마다 재연결을 시도 한다.
	
- UI 변경

	- 변경사항 : 디바이스 연결하는 화면에서 가장 최근에 연결되었던 디바이스 리스트의 오른쪽 하단에 'Last paired'로 표시
	<div align = "center">
	<img src="https://github.com/Marveldex/MDEX_SEAT_31a1/blob/master/Image/selectdevice.jpg" />
	</div>	
	                                                                      
	                                             
	- 변경사항 : 상단에 BLE연결 상태 표시와 하단에 추가된 자세정보 추가
		- BLE 정보
			- Receiving : BLE연결 상태 양호
			- Blind : BLE연결 상태 끊어짐
			
		- 자세 정보
			- left leg : 왼쪽 다리 부착 여부
			- right leg :  오른쪽 다리 부착 여부
			- longitudinal : 착석의 앞뒤 기울기
			- Leteral : 좌우 기울기
			                                                 
			
	<div align = "center">
	<img src="https://github.com/Marveldex/MDEX_SEAT_31a1/blob/master/Image/changedUI.jpg" />
	</div>
	
	
---------------------------------------
2017-10-26

수정사항
 - 문제 : 화면의 비율에 따라 Application의 UI가 보이지 않는 현상
 - 해결 : UI에서 가장 큰 비율을 차지하는 상세 리스트를 버튼으로 컨트롤하여 'SHOW'와 'HIDE' 효과를 나누어 출력 여부를 사용자가 결정

---------------------------------------

### Note
- Android 4.3 or later is required.
- Android Studio supported 
