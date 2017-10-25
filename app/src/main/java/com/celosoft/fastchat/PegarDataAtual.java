package com.celosoft.fastchat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Marcelo on 12/04/2017.
 */

public final class PegarDataAtual {


    public static String  dataPost() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
        Date data = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        Date data_atual = cal.getTime();

        String dataPost = dateFormat.format(data_atual);

        return dataPost;

    }

    public static String diaHoraPost() {

        SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM HH:mm");
        Date data1 = new Date();
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(data1);
        Date dataAgora = cal1.getTime();

        String diaHora = dateFormat1.format(dataAgora);

        return diaHora;

    }

    public static String  dataAtualCompleta() {


        SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yy");
        Date data1 = new Date();
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(data1);
        Date dataAgora = cal1.getTime();

        String dataAtualCompleta = dateFormat1.format(dataAgora);

        return dataAtualCompleta;

    }

}
