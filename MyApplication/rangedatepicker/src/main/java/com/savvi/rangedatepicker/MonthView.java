package com.savvi.rangedatepicker;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MonthView extends LinearLayout {
    TextView title;
    CalendarGridView grid;
    private Listener listener;
    private List<CalendarCellDecorator> decorators;
    private boolean isRtl;
    private Locale locale;

    ArrayList<Integer> deactivatedDates;

    public static MonthView create(ViewGroup parent, LayoutInflater inflater,
                                   DateFormat weekdayNameFormat, Listener listener, Calendar today, int dividerColor,
                                   int dayBackgroundResId, int dayTextColorResId, int titleTextColor, boolean displayHeader,
                                   int headerTextColor, Locale locale, DayViewAdapter adapter) {
        return create(parent, inflater, weekdayNameFormat, listener, today, dividerColor,
                dayBackgroundResId, dayTextColorResId, titleTextColor, displayHeader, headerTextColor, null,
                locale, adapter);
    }

    public static MonthView create(ViewGroup parent, LayoutInflater inflater,
                                   DateFormat weekdayNameFormat, Listener listener, Calendar today, int dividerColor,
                                   int dayBackgroundResId, int dayTextColorResId, int titleTextColor, boolean displayHeader,
                                   int headerTextColor, List<CalendarCellDecorator> decorators, Locale locale,
                                   DayViewAdapter adapter) {
        final MonthView view = (MonthView) inflater.inflate(R.layout.month, parent, false);
        view.setDayViewAdapter(adapter);
        view.setDividerColor(dividerColor);
        view.setDayTextColor(dayTextColorResId);
        view.setTitleTextColor(titleTextColor);
        view.setDisplayHeader(displayHeader);
        view.setHeaderTextColor(headerTextColor);

        if (dayBackgroundResId != 0) {
            view.setDayBackground(dayBackgroundResId);
        }

        final int originalDayOfWeek = today.get(Calendar.DAY_OF_WEEK);

        view.isRtl = isRtl(locale);
        view.locale = locale;
        int firstDayOfWeek = today.getFirstDayOfWeek();
        final CalendarRowView headerRow = (CalendarRowView) view.grid.getChildAt(0);
        for (int offset = 0; offset < 7; offset++) {
            today.set(Calendar.DAY_OF_WEEK, getDayOfWeek(firstDayOfWeek, offset, view.isRtl));
            final TextView textView = (TextView) headerRow.getChildAt(offset);
            textView.setText(weekdayNameFormat.format(today.getTime()));
        }
        today.set(Calendar.DAY_OF_WEEK, originalDayOfWeek);
        view.listener = listener;
        view.decorators = decorators;
        return view;
    }

    private static int getDayOfWeek(int firstDayOfWeek, int offset, boolean isRtl) {
        int dayOfWeek = firstDayOfWeek + offset;
        if (isRtl) {
            return 8 - dayOfWeek;
        }
        return dayOfWeek;
    }

    private static boolean isRtl(Locale locale) {
        // TODO convert the build to gradle and use getLayoutDirection instead of this (on 17+)?
        final int directionality = Character.getDirectionality(locale.getDisplayName(locale).charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT
                || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDecorators(List<CalendarCellDecorator> decorators) {
        this.decorators = decorators;
    }

    public List<CalendarCellDecorator> getDecorators() {
        return decorators;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        title = (TextView) findViewById(R.id.title);
        grid = (CalendarGridView) findViewById(R.id.calendar_grid);
    }

    public void init(MonthDescriptor month, List<List<MonthCellDescriptor>> cells,
                     boolean displayOnly, Typeface titleTypeface, Typeface dateTypeface, ArrayList<Date> deactivatedDates, @Nullable ArrayList<SubTitle> subTitles)  {

        //TODO:Modificando este metodo
        Logr.d("Initializing MonthView (%d) for %s", System.identityHashCode(this), month);
        long start = System.currentTimeMillis();
        title.setText(month.getLabel());
        NumberFormat numberFormatter = NumberFormat.getInstance(locale);

        final int numRows = cells.size();
        grid.setNumRows(numRows);
        //formateos de las fechas
        SimpleDateFormat formatFrom = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        SimpleDateFormat formatTo = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        String formattedDateofWeek;
        for (int i = 0; i < 6; i++) {
            CalendarRowView weekRow = (CalendarRowView) grid.getChildAt(i + 1);
            weekRow.setListener(listener);
            if (i < numRows) {
                weekRow.setVisibility(VISIBLE);
                List<MonthCellDescriptor> week = cells.get(i);
                for (int c = 0; c < week.size(); c++) {
                    MonthCellDescriptor cell = week.get(isRtl ? 6 - c : c);
                    CalendarCellView cellView = (CalendarCellView) weekRow.getChildAt(c);

                    String cellDate = numberFormatter.format(cell.getValue());
                    Log.i("init","Init MonthView cell: "+cell.getDate());
                    if (!cellView.getDayOfMonthTextView().getText().equals(cellDate)) {
                        cellView.getDayOfMonthTextView().setText(cellDate);
                    }

                    final SubTitle subTitle = SubTitle.getByDate(subTitles, cell.getDate());

                    if (subTitle != null && !cellView.getSubTitleTextView().getText().equals(subTitle.getTitle())) {
                        cellView.getSubTitleTextView().setText(subTitle.getTitle());
                    }

                    cellView.setEnabled(cell.isCurrentMonth());
                    Date dayOfWeek = cell.getDate();
                    //tranformar dayOfWeek al formato requerido
                    try{
                        String daisOfWeek = dayOfWeek.toString();
                        Date reformatDayOfWeek =  formatFrom.parse(daisOfWeek);
                        formattedDateofWeek  = formatTo.format(reformatDayOfWeek);//valor que hay que usar

                    }catch (ParseException e) {
                        Log.e("ParseException", "Exception to parse in init transformacion dayOfWeek: " + e);
                        throw new RuntimeException(e);
                    }

                    List<String> formatDeactivatedDates = new ArrayList<>();
                    //transformar la lista de fechas desactivadas a formato adecuado
                    for(Date date : deactivatedDates){
                        try{
                            //date corresponde con la fecha en el bucle for de todas las fechas desactivadas
                            //formateo al necesario a day
                            String dateValue = date.toString();
                            Date reformatDate =  formatFrom.parse(dateValue);
                            String formattedDate = formatTo.format(reformatDate).replace("39","20"); //valor que hay que usar
                            formatDeactivatedDates.add(formattedDate);
                        } catch (ParseException e) {
                            Log.e("ParseException", "Exception to parse in init for deactivatedDates: "+e);
                            throw new RuntimeException(e);
                        }
                    }
                    //if-else para formatear las celdas
                    if(formatDeactivatedDates.contains(formattedDateofWeek)){
                        for(String dateFormat : formatDeactivatedDates){
                            Log.i("","Entra en if");
                            cellView.setSelectable(true);
                            cellView.setSelected(cell.isSelected());//esta seleccionado???
                            cellView.setCurrentMonth(cell.isCurrentMonth());
                            cellView.setToday(cell.isToday());
                            cellView.setRangeState(cell.getRangeState());
                            cellView.setHighlighted(cell.isHighlighted());
                            cellView.setRangeUnavailable(cell.isUnavailable());
                            cellView.setDeactivated(false);
                        }
                    }else{
                        Log.i("","Entra en else formatDeactivatedDates.contains(formattedDateofWeek)");
                        cellView.setSelectable(true);
                        cellView.setSelected(cell.isSelected());//esta seleccionado???
                        cellView.setCurrentMonth(cell.isCurrentMonth());
                        cellView.setToday(cell.isToday());
                        cellView.setRangeState(cell.getRangeState());
                        cellView.setHighlighted(cell.isHighlighted());
                        cellView.setRangeUnavailable(cell.isUnavailable());
                        cellView.setDeactivated(true);
                    }


                   /* for(Date date : deactivatedDates){
                        try{
                        //dayOfWeek se corresponde con el dia de la celda que corresponda
                        //formateo al necesario a dayofWeek
                            String daisOfWeek = dayOfWeek.toString();
                            Date reformatDayOfWeek =  formatFrom.parse(daisOfWeek);
                            String formattedDateofWeek = formatTo.format(reformatDayOfWeek);//valor que hay que usar
                            //date corresponde con la fecha en el bucle for de todas las fechas desactivadas
                        //formateo al necesario a day
                            String dateValue = date.toString();
                            Date reformatDate =  formatFrom.parse(dateValue);
                            String formattedDate = formatTo.format(reformatDate).replace("39","20"); //valor que hay que usar
                            Log.d("info","FormattedDateofWeek: "+formattedDateofWeek+" FormattedDate: "+formattedDate);
                        //condicion para ponerlo como desactivado o normal
                            if (!formattedDate.equals(formattedDateofWeek)) {
                                Log.i("","Entra en if");
                                cellView.setSelectable(true);
                                cellView.setSelected(cell.isSelected());//esta seleccionado???
                                cellView.setCurrentMonth(cell.isCurrentMonth());
                                cellView.setToday(cell.isToday());
                                cellView.setRangeState(cell.getRangeState());
                                cellView.setHighlighted(cell.isHighlighted());
                                cellView.setRangeUnavailable(cell.isUnavailable());
                                cellView.setDeactivated(true);
                            }
                            else {
                                Log.i("","Entra en else");
                                cellView.setSelectable(cell.isSelectable());
                                cellView.setSelected(cell.isSelected());
                                cellView.setCurrentMonth(cell.isCurrentMonth());
                                cellView.setToday(cell.isToday());
                                cellView.setRangeState(cell.getRangeState());
                                cellView.setHighlighted(cell.isHighlighted());
                                cellView.setRangeUnavailable(cell.isUnavailable());
                                cellView.setDeactivated(false);
                            }
                        } catch (ParseException e) {
                            Log.e("ParseException", "Exception to parse in init: "+e);
                            throw new RuntimeException(e);
                        }
                    }*/
                    cellView.setTag(cell);

                    if (null != decorators) {
                        for (CalendarCellDecorator decorator : decorators) {
                            decorator.decorate(cellView, cell.getDate());
                        }
                    }
                }
            } else {
                weekRow.setVisibility(GONE);
            }
        }

        if (titleTypeface != null) {
            title.setTypeface(titleTypeface);
        }
        if (dateTypeface != null) {
            grid.setTypeface(dateTypeface);
        }

        Logr.d("MonthView.init took %d ms", System.currentTimeMillis() - start);
    }

    public void setDividerColor(int color) {
        grid.setDividerColor(color);
    }

    public void setDayBackground(int resId) {
        grid.setDayBackground(resId);
    }

    public void setDayTextColor(int resId) {
        grid.setDayTextColor(resId);
    }

    public void setDayViewAdapter(DayViewAdapter adapter) {
        grid.setDayViewAdapter(adapter);
    }

    public void setTitleTextColor(int color) {
        title.setTextColor(color);
    }

    public void setDisplayHeader(boolean displayHeader) {
        grid.setDisplayHeader(displayHeader);
    }

    public void setHeaderTextColor(int color) {
        grid.setHeaderTextColor(color);
    }

    public interface Listener {
        void handleClick(MonthCellDescriptor cell);
    }
}
