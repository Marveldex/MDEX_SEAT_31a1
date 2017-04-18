package com.nordicsemi.venusAlpha01a;

import android.graphics.Rect;

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
    public static byte def_CELL_COUNT_ROW0 = 6;
    public static byte def_CELL_COUNT_ROW1 = 15;
    public static byte def_CELL_COUNT_ROW2 = 10;

    public static byte [] nPressureValue_Row0 = new byte [def_CELL_COUNT_ROW0];
    public static byte [] nPressureValue_Row1 = new byte [def_CELL_COUNT_ROW1];
    public static byte [] nPressureValue_Row2 = new byte [def_CELL_COUNT_ROW2];

    final byte [] packet_data_32bit = new byte[20];

    public static final byte [] adcValue_M = new byte[20]; // ADC of Main
    public static final byte [] adcValue_S = new byte[20]; // ADC of Shield

    public static boolean m_isPacketCompleted = false;

    private static int def_THRESHOLD_VALID_LOWEST = 5;
    private static int def_THRESHOLD_VALUE_SEAT_OCCUPIED = 5;//200;

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
        if( packet_data_32bit[0] == 'M'){
            textHexaMain = String.format("Ma: %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d",
                    packet_data_32bit[1], packet_data_32bit[2], packet_data_32bit[3], packet_data_32bit[4], packet_data_32bit[5], packet_data_32bit[6], packet_data_32bit[7], packet_data_32bit[8],
                    packet_data_32bit[9], packet_data_32bit[10], packet_data_32bit[11], packet_data_32bit[12], packet_data_32bit[13], packet_data_32bit[14], packet_data_32bit[15], packet_data_32bit[16]);

            System.arraycopy(packet_data_32bit, 1, adcValue_M, 0, 16); // 1: offset of source, 0: offset of dest, 16: length)

            m_isPacketCompleted = false;
        }
        else if( packet_data_32bit[0] == 'S') {
            textHexaShield = String.format("Sh: %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d",
                    packet_data_32bit[1], packet_data_32bit[2], packet_data_32bit[3], packet_data_32bit[4], packet_data_32bit[5], packet_data_32bit[6], packet_data_32bit[7], packet_data_32bit[8],
                    packet_data_32bit[9], packet_data_32bit[10], packet_data_32bit[11], packet_data_32bit[12], packet_data_32bit[13], packet_data_32bit[14], packet_data_32bit[15], packet_data_32bit[16]);
            System.arraycopy(packet_data_32bit, 1, adcValue_S, 0, 16); // 1: offset of source, 0: offset of dest, 16: length)

            m_isPacketCompleted = true;
        }

        //  3) Reorder sensor sequence after last packet ('S') arrived.
        if( m_isPacketCompleted == true) {
            reorderSensorSequence();
        }

//      parse battery level
        m_BatteryLevel = packet_data_32bit[17];

    }

    private void reorderSensorSequence(){
        int cell_index;

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

    public static boolean isPacketCompleted(){
        return m_isPacketCompleted;
    }

    public static byte getBatteryLevel(){
        return m_BatteryLevel;
    }

}
