package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.savvi.rangedatepicker.CalendarPickerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyCalendarPicker extends DialogFragment {
    private Button cancelButton;
    private Button acceptButton;

    private TextView mTagsFechas;

    private View view;

    private CalendarPickerView calendar;

    private ArrayList<Date> startSelectedDates = new ArrayList<>();

    public ArrayList<String> datesToAdd = new ArrayList<>();


    private Bundle savedState;

    public MyCalendarPicker(TextView mTagsFechas) {
        this.mTagsFechas = mTagsFechas;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_multi_tap_calendar_view, container, false);
        acceptButton = view.findViewById(R.id.cps_accept_button);
        cancelButton = view.findViewById(R.id.cps_cancel_button);
        calendar = view.findViewById(R.id.calendar_view);
        initCalendar(datesToAdd);


        return view;
    }


    public void initCalendar(List<String> fechasAñadidas){
        //limite minimo de años
        Calendar pastYear = Calendar.getInstance();
        pastYear.add(Calendar.YEAR, -10);
        //limite maximo de años
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR,10);
        if(fechasAñadidas.size() != 0){
            for(int i=0;i<fechasAñadidas.size();i++){
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    Date fecha = sdf.parse(fechasAñadidas.get(i));
                    startSelectedDates.add(fecha);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }


            calendar
                    .init(pastYear.getTime(),nextYear.getTime(),new SimpleDateFormat("MMMM yyyy", Locale.getDefault()))
                    .withHighlightedDates(startSelectedDates)
                    .inMode(CalendarPickerView.SelectionMode.MULTIPLE);
        }else{
            calendar
                    .init(pastYear.getTime(),nextYear.getTime(),new SimpleDateFormat("MMMM yyyy", Locale.getDefault()))
                    .inMode(CalendarPickerView.SelectionMode.MULTIPLE);

        }

        calendar.scrollToDate(new Date());

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Info","Fechas seleccionadas: "+calendar.getSelectedDates());

                for(int i=0;i<calendar.getSelectedDates().size();i++){
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    String fecha = sdf.format(calendar.getSelectedDates().get(i));
                    mTagsFechas.setText(fecha);
                }

                savedState = new Bundle();
                onSaveInstanceState(savedState);
                dismiss();

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savedState = new Bundle();
                onSaveInstanceState(savedState);
                dismiss();
            }
        });
    }


}
