package com.marveldex.seat31;

/**
 * Created by sehjin12-pc on 2016-12-11.
 */


/**
 *
 * @details 압력세서로부터 전달 받은 데이터를 처리하여 직관적인 값으로 변환하고 사용할 변수에 Setting
 * @author Marveldex
 * @date 2017-03-17
 * @version 0.0.1
 * @li list1
 * @li list2
 *
 */

public class PacketParser {
    public static int def_PACKET_LENGTH = 20;
    public static byte m_BatteryLevel = 0;
    public static byte m_Reserved = 0; // empty
    public static byte m_HWState = 0; // 19th, 0 bit
    public static byte def_CELL_COUNT_ROW0 = 6;
    public static byte def_CELL_COUNT_ROW1 = 15;
    public static byte def_CELL_COUNT_ROW2 = 10;

    public static byte [] nPressureValue_Row0 = new byte [def_CELL_COUNT_ROW0];
    public static byte [] nPressureValue_Row1 = new byte [def_CELL_COUNT_ROW1];
    public static byte [] nPressureValue_Row2 = new byte [def_CELL_COUNT_ROW2];

    final byte [] packet_data_32bit = new byte[20];

    public static final byte [] adcValue_M = new byte[20]; // ADC of Main
    public static final byte [] adcValue_S = new byte[20]; // ADC of Shield

    public static final byte [] adcValue_L = new byte[20]; // ADC of Shield
    public static final byte [] adcValue_R = new byte[20]; // ADC of Shield
    public static String Mode_Info = null;

    public static boolean m_isPacketCompleted = false;

    private static int def_THRESHOLD_VALID_LOWEST = 5;
    private static int def_THRESHOLD_VALUE_SEAT_OCCUPIED = 200;
    private static int def_THRESHOLD_VALUE_ONE_LEG_EMPTY = 25;

    public static String textHexaMain = " ";
    public static String textHexaShield = " ";


    public PacketParser(){
    }

    public static byte getSensorDataByCoord(int nRowIndex, int nColIndex){
        switch (nRowIndex){
            case 0:
                return nPressureValue_Row0[nColIndex];
            case 1:
                return nPressureValue_Row1[nColIndex];
            case 2:
                return nPressureValue_Row2[nColIndex];
        }

        return 0;
    }

    /**
     *
     * @brief    전달 받은 압력 값을 구분하고 해당 값들을 변수에 Setting
     * @details 1) Convert 8bit to 32bit, 2) Distinguish what if this packet is 'M' or 'S' and store to buffer 3) Reorder sensor sequence after last packet ('S') arrived.
     * @param
     * @return
     * @throws
     */

    public void onReceiveRawPacket(byte [] packet_raw_data){
        //  1) Convert 8bit to 32bit
        for(int index = 0 ; index < 20 ; index++){
            packet_data_32bit[index] = packet_raw_data[index];// & 0xff;
        }


        //  2) Distinguish what if this packet is 'M' or 'S' and store to buffer
        //  whole packet is 31byte long. It's divided 2 packets. 'M' is first, 'S' is last. So complete packet is assembled when 'S' is arrived.
        //해당 부분 수정 하면됨
        if( packet_data_32bit[0] == 'M'){
            textHexaMain = String.format("Ma: %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d",
                    packet_data_32bit[1], packet_data_32bit[2], packet_data_32bit[3], packet_data_32bit[4], packet_data_32bit[5], packet_data_32bit[6], packet_data_32bit[7], packet_data_32bit[8],
                    packet_data_32bit[9], packet_data_32bit[10], packet_data_32bit[11], packet_data_32bit[12], packet_data_32bit[13], packet_data_32bit[14], packet_data_32bit[15], packet_data_32bit[16]);

            System.arraycopy(packet_data_32bit, 1, adcValue_M, 0, 16); // 1: offset of source, 0: offset of dest, 16: length)

            m_isPacketCompleted = false;
            Mode_Info = "M";
        }
        else if( packet_data_32bit[0] == 'S') {
            textHexaShield = String.format("Sh: %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d",
                    packet_data_32bit[1], packet_data_32bit[2], packet_data_32bit[3], packet_data_32bit[4], packet_data_32bit[5], packet_data_32bit[6], packet_data_32bit[7], packet_data_32bit[8],
                    packet_data_32bit[9], packet_data_32bit[10], packet_data_32bit[11], packet_data_32bit[12], packet_data_32bit[13], packet_data_32bit[14], packet_data_32bit[15], packet_data_32bit[16]);
            System.arraycopy(packet_data_32bit, 1, adcValue_S, 0, 16); // 1: offset of source, 0: offset of dest, 16: length)

            m_isPacketCompleted = true;
            Mode_Info = "S";

        }else if(packet_data_32bit[0] == 'L') {

            textHexaMain = String.format("Ma: %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d",
                    packet_data_32bit[1], packet_data_32bit[2], packet_data_32bit[3], packet_data_32bit[4], packet_data_32bit[5], packet_data_32bit[6], packet_data_32bit[7], packet_data_32bit[8],
                    packet_data_32bit[9], packet_data_32bit[10], packet_data_32bit[11], packet_data_32bit[12], packet_data_32bit[13], packet_data_32bit[14], packet_data_32bit[15], packet_data_32bit[16]);

            System.arraycopy(packet_data_32bit, 1, adcValue_L, 0, 16); // 1: offset of source, 0: offset of dest, 16: length)

            m_isPacketCompleted = false;
            Mode_Info = "L";

        }else if(packet_data_32bit[0] == 'R') {

            textHexaShield = String.format("Sh: %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d",
                    packet_data_32bit[1], packet_data_32bit[2], packet_data_32bit[3], packet_data_32bit[4], packet_data_32bit[5], packet_data_32bit[6], packet_data_32bit[7], packet_data_32bit[8],
                    packet_data_32bit[9], packet_data_32bit[10], packet_data_32bit[11], packet_data_32bit[12], packet_data_32bit[13], packet_data_32bit[14], packet_data_32bit[15], packet_data_32bit[16]);
            System.arraycopy(packet_data_32bit, 1, adcValue_R, 0, 16); // 1: offset of source, 0: offset of dest, 16: length)

            m_isPacketCompleted = true;
            Mode_Info = "R";

        }

        //  3) Reorder sensor sequence after last packet ('S') arrived.
        if( m_isPacketCompleted == true) {
            reorderSensorSequence();
        }

//      parse battery level
        m_BatteryLevel = packet_data_32bit[17];

        //  18th : Empty, reserved
        m_Reserved = packet_data_32bit[18];

        //  19th (Last) : Venus Hardware setting. i.e. dip switch setting
        m_HWState = packet_data_32bit[19];

    }

    private void reorderSensorSequence(){
        int cell_index;

        if(packet_data_32bit[0] == 'S'){

            cell_index = 0;
            nPressureValue_Row0[cell_index++] = adcValue_M[10];
            nPressureValue_Row0[cell_index++] = adcValue_M[12];
            nPressureValue_Row0[cell_index++] = adcValue_M[14];
            nPressureValue_Row0[cell_index++] = adcValue_S[0];
            nPressureValue_Row0[cell_index++] = adcValue_S[2];
            nPressureValue_Row0[cell_index] = adcValue_S[4];

            cell_index = 0;
            nPressureValue_Row1[cell_index++] = adcValue_M[5];
            nPressureValue_Row1[cell_index++] = adcValue_M[6];
            nPressureValue_Row1[cell_index++] = adcValue_M[7];
            nPressureValue_Row1[cell_index++] = adcValue_M[8];
            nPressureValue_Row1[cell_index++] = adcValue_M[9];
            nPressureValue_Row1[cell_index++] = adcValue_M[11];
            nPressureValue_Row1[cell_index++] = adcValue_M[13];
            nPressureValue_Row1[cell_index++] = adcValue_M[15];
            nPressureValue_Row1[cell_index++] = adcValue_S[1];
            nPressureValue_Row1[cell_index++] = adcValue_S[3];
            nPressureValue_Row1[cell_index++] = adcValue_S[5];
            nPressureValue_Row1[cell_index++] = adcValue_S[6];
            nPressureValue_Row1[cell_index++] = adcValue_S[7];
            nPressureValue_Row1[cell_index++] = adcValue_S[8];
            nPressureValue_Row1[cell_index] = adcValue_S[9];

            cell_index = 0;
            nPressureValue_Row2[cell_index++] = adcValue_M[0];
            nPressureValue_Row2[cell_index++] = adcValue_M[1];
            nPressureValue_Row2[cell_index++] = adcValue_M[2];
            nPressureValue_Row2[cell_index++] = adcValue_M[3];
            nPressureValue_Row2[cell_index++] = adcValue_M[4];
            nPressureValue_Row2[cell_index++] = adcValue_S[10];
            nPressureValue_Row2[cell_index++] = adcValue_S[11];
            nPressureValue_Row2[cell_index++] = adcValue_S[12];
            nPressureValue_Row2[cell_index++] = adcValue_S[13];
            nPressureValue_Row2[cell_index] = adcValue_S[14];

        }else if(packet_data_32bit[0] == 'R'){

            cell_index = 0;
            nPressureValue_Row0[cell_index++] = adcValue_L[10];
            nPressureValue_Row0[cell_index++] = adcValue_L[12];
            nPressureValue_Row0[cell_index++] = adcValue_L[14];
            nPressureValue_Row0[cell_index++] = adcValue_R[0];
            nPressureValue_Row0[cell_index++] = adcValue_R[2];
            nPressureValue_Row0[cell_index] = adcValue_R[4];

            cell_index = 0;
            nPressureValue_Row1[cell_index++] = adcValue_L[5];
            nPressureValue_Row1[cell_index++] = adcValue_L[6];
            nPressureValue_Row1[cell_index++] = adcValue_L[7];
            nPressureValue_Row1[cell_index++] = adcValue_L[8];
            nPressureValue_Row1[cell_index++] = adcValue_L[9];
            nPressureValue_Row1[cell_index++] = adcValue_L[11];
            nPressureValue_Row1[cell_index++] = adcValue_L[13];
            nPressureValue_Row1[cell_index++] = adcValue_L[15];
            nPressureValue_Row1[cell_index++] = adcValue_R[1];
            nPressureValue_Row1[cell_index++] = adcValue_R[3];
            nPressureValue_Row1[cell_index++] = adcValue_R[5];
            nPressureValue_Row1[cell_index++] = adcValue_R[6];
            nPressureValue_Row1[cell_index++] = adcValue_R[7];
            nPressureValue_Row1[cell_index++] = adcValue_R[8];
            nPressureValue_Row1[cell_index] = adcValue_R[9];

            cell_index = 0;
            nPressureValue_Row2[cell_index++] = adcValue_L[0];
            nPressureValue_Row2[cell_index++] = adcValue_L[1];
            nPressureValue_Row2[cell_index++] = adcValue_L[2];
            nPressureValue_Row2[cell_index++] = adcValue_L[3];
            nPressureValue_Row2[cell_index++] = adcValue_L[4];
            nPressureValue_Row2[cell_index++] = adcValue_R[10];
            nPressureValue_Row2[cell_index++] = adcValue_R[11];
            nPressureValue_Row2[cell_index++] = adcValue_R[12];
            nPressureValue_Row2[cell_index++] = adcValue_R[13];
            nPressureValue_Row2[cell_index] = adcValue_R[14];

        }

    }


    public static boolean isSeatOccupied(){
        int summation = 0;
        for(int i = 0 ; i < def_CELL_COUNT_ROW1 ; i++) {
            summation += nPressureValue_Row1[i];

            if(def_THRESHOLD_VALUE_SEAT_OCCUPIED < summation)
                return true;
        }

        return false;
    }


    public static boolean isPacketCompleted(){
        return m_isPacketCompleted;
    }

    public static byte getBatteryLevel(){
        return m_BatteryLevel;
    }

    public static boolean getHW_DIPSW_state(int dipsw_index) {
        //  valid dipsw_index value is 0, 1, 2 only.
        if( (dipsw_index < 0) || (2 < dipsw_index) ) {
            return false;
        }

        boolean ret_val = false;
        //  Venus source code...
        // g_ucNUS_PacketBuffer[19] = (g_DIPSWITCH_state[0] << 7) | (g_DIPSWITCH_state[1] << 6) | (g_DIPSWITCH_state[2] << 5);
        switch (dipsw_index) {
            case 0: // 0b1000000 = 0x80
                ret_val = ( ((m_HWState & 0x80) >> 7) == 0x01);
                break;
            case 1:
                ret_val = ( ((m_HWState & 0x40) >> 6) == 0x01);
                break;
            case 2:
                ret_val = ( ((m_HWState & 0x20) >> 5) == 0x01);
                break;
        }

        return ret_val;
    }

    /**
     *
     * @brief calculate lateral COM
     * @details
     * @param
     * @return
     */
    public static float getLateralCOM_Row1(){
        int summation_mess = 0;
        float summation_mess_x_position = 0;
        float position;

        for(int i = 0 ; i < def_CELL_COUNT_ROW1 ; i++) {
            position = (float)i;
            summation_mess_x_position += (nPressureValue_Row1[i] * position);
            summation_mess += nPressureValue_Row1[i];
        }

        float position_lateral_COM = summation_mess_x_position / summation_mess;

        return (position_lateral_COM - 7.0f); // 7.0f means center position sensor of 1st row has index of 7. So 7 is center coordnate.
    }


    public static float getLateralCOC_left_Row1() {
        int cell_index = 0;
        for(cell_index = 0 ; cell_index < def_CELL_COUNT_ROW1 ; cell_index++) {
            if(def_THRESHOLD_VALID_LOWEST < nPressureValue_Row1[cell_index]) {
                break;
            }
        }
        if(cell_index == def_CELL_COUNT_ROW1)
            cell_index = 0;

        float coord_left = (float)cell_index - 7.0F; // 7.0F is center point of row 1
        //float coord_left = (float)cell_index;

        return coord_left;
    }

    public static float getLateralCOC_right_Row1() {
        int cell_index = 0;
        for(cell_index = (def_CELL_COUNT_ROW1 - 1) ; 0 <= cell_index ; cell_index--) {
            if(def_THRESHOLD_VALID_LOWEST < nPressureValue_Row1[cell_index]) {
                break;
            }
        }
        if(cell_index == 0)
            cell_index = def_CELL_COUNT_ROW1 - 1;

        float coord_right = (float)cell_index - 7.0F; // 7.0F is center point of row 1
        //float coord_right = (float)cell_index;

        return coord_right;
    }

    //-------------------------------------------------------------------------
    //  Posture determination
    //-------------------------------------------------------------------------
    public static int getLeftLegPressureSum() {
        int sum_value_left = 0;

        //  left : 0~2 cells of row 0
        for(int cell_index = 0 ; cell_index <= 2 ; cell_index++) {
            sum_value_left += nPressureValue_Row0 [cell_index];
        }
        return sum_value_left;
    }

    public static int getRightLegPressureSum() {
        int sum_value_right = 0;

        //  right : 3~5 cells of row 0
        for(int cell_index = 3 ; cell_index <= 5 ; cell_index++) {
            sum_value_right += nPressureValue_Row0 [cell_index];
        }
        return sum_value_right;
    }

    public static boolean isLeftLeg_StuckOnChair() {
        if(isSeatOccupied() == false)
            return false;

        if( def_THRESHOLD_VALUE_ONE_LEG_EMPTY < getLeftLegPressureSum() ){
            return true;
        }

        return false;
    }

    public static boolean isRightLeg_StuckOnChair() {
        if(isSeatOccupied() == false)
            return false;

        if( def_THRESHOLD_VALUE_ONE_LEG_EMPTY < getRightLegPressureSum() ){
            return true;
        }

        return false;
    }

    public static float getLongitudinalVector(){
        final float def_WEIGHT_FRONT = 1.2f; // front cell has more weight. Distance from row0 to row1 is further than distance from row1 to row2. row1 is center.
        int cell_value = 0;

        //  average of front : row 0
        int cell_valid_count_front = 0;
        int sum_value_front = 0;
        float average_front = 0.0f;

        for(int cell_index = 0 ; cell_index < def_CELL_COUNT_ROW0 ; cell_index++) {
            cell_value = nPressureValue_Row0 [cell_index];
            sum_value_front += cell_value;

            if(def_THRESHOLD_VALID_LOWEST < cell_value) {
                cell_valid_count_front++;
            }
        }

        if( 0 < cell_valid_count_front) {
            average_front = sum_value_front / cell_valid_count_front;
        }

        average_front *= def_WEIGHT_FRONT;

        //  average of back : row 2
        int cell_valid_count_back = 0;
        int sum_value_back = 0;
        float average_back= 0.0f;

        for(int cell_index = 0 ; cell_index < def_CELL_COUNT_ROW2 ; cell_index++) {
            cell_value = nPressureValue_Row2 [cell_index];
            sum_value_back += cell_value;

            if(def_THRESHOLD_VALID_LOWEST < cell_value) {
                cell_valid_count_back++;
            }
        }

        if( 0 < cell_valid_count_back) {
            average_back = sum_value_back / cell_valid_count_back;
        }

        //  calculate logitudinal vector
        float longitudinal_vector = ( ( average_front * -1) + (average_back * 1 ) ) / (average_front + average_back); // -1 and 1 is y coordnate of row0(front) and row2(back).

        return longitudinal_vector;
    }

    public static float getLateralVector() {
        float center_of_mess = getLateralCOM_Row1();
        float left_of_contour = getLateralCOC_left_Row1();
        float right_of_contour = getLateralCOC_right_Row1();

        float center_of_heap = (left_of_contour + right_of_contour) / 2;

        float lateral_vector = center_of_mess - center_of_heap;

        return lateral_vector;
    }


}
