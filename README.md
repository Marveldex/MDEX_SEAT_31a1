# MDEX_SEAT_31a1

This is Sample Application for MDEX_VENUS board & sensor test

It can be operated only in Android environment.

we use BLE 4.0 communication

================================================================

노르딕 nrfUART 소스코드 주소
https://github.com/hubuhubu/Android-nRF-UART

Main UI 
	
- UI 설명
			
- 강조 사항 : 신규 기능 사항. 데이터 수신 상태 표시, 자세 분석 사항.
<div align = "center">
<img src="https://github.com/Marveldex/MDEX_SEAT_31a1/blob/master/Image/changedUI.jpg" />
</div>

	- 데이터 수신 상태
		- Receiving : 데이터 수신 상태 양호
		- Blind : 데이터 수신 상태 불량
		- Last Blind : Blind가 발생한 시간 및 경과시간
		
	- 무게중심 정보
		- COC : Center of Contour(착석윤곽의 중심)
		- COM : Center of Mess(무게 중심)
		
	- 자세 정보
		- left leg : 왼쪽 다리 부착 여부
		- right leg :  오른쪽 다리 부착 여부
		- longitudinal : 앞뒤 무게중심 값
		- lateral : 좌우 무게중심 값
		
- 강조 사항 : 디바이스 연결하는 화면에서 가장 최근에 연결되었던 디바이스를 파란색으로 'Last device'로 표시
<div align = "center">
<img src="https://github.com/Marveldex/MDEX_SEAT_31a1/blob/master/Image/selectdevice.jpg" />
</div>	
																	   
- BLE 연결
	- 강조 사항 : BLE연결이 끊어졌을 경우 재연결을 시도한다.
		- 자동 재연결하는 경우
			- 비너스 보드의 전원이 꺼지거나 폰과의 거리가 멀어질 경우
		- 자동 재연결하지 않는 경우
			- 폰을 껐다 켜는 경우
			- 폰의 블루투스 기능을 껐다 켜는 경우
			- 폰의 어플을 끄는 경우


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
 - 블루투스의 자동 재연결 옵션 추가. 비너스 보드 단과 연결이 끊어질 경우, 폰에서 재연결을 시도한다. 재연결을 시도하는 동안 BLE상태는 'Receiving'에서 'Blind'로 바뀐다.
 - 신규 기능 추가. 데이터 수신 상태 표시, 자세 분석 추가.
 - 디바이스 연결하는 화면에서 가장 최근에 연결되었던 디바이스를 파란색으로 'Last device'로 표시

	
---------------------------------------
2017-10-26

수정사항
 - 문제 : 화면의 비율에 따라 Application의 UI가 보이지 않는 현상
 - 해결 : UI에서 가장 큰 비율을 차지하는 상세 리스트를 버튼으로 컨트롤하여 'SHOW'와 'HIDE' 효과를 나누어 출력 여부를 사용자가 결정

---------------------------------------

### Note
- Android 4.3 or later is required.
- Android Studio supported 
