package or.kr.ajouhosp.stroke;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import or.kr.ajouhosp.stroke.activity.LivePreviewActivity;
import or.kr.ajouhosp.stroke.activity.MyMarkerView;

public class ChartActivity extends Activity{

    private LineChart lineChart;
    private static List<String> stringList = new ArrayList<>(); //날짜 배열
    private static List<Entry> entries = new ArrayList<>();     //갯수
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        stringList.clear();
        entries.clear();
        super.onCreate(savedInstanceState);
        readFile();
        setContentView(R.layout.chart_activity);
        lineChart = (LineChart)findViewById(R.id.chart);
        LineDataSet lineDataSet = new LineDataSet(entries, "갯수");
        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleRadius(6);
        lineDataSet.setCircleColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setCircleColorHole(Color.BLUE);
        lineDataSet.setColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawValues(false);
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.enableGridDashedLine(7, 24, 0);
        xAxis.setGranularityEnabled(true); //x축값 중복 문제 해결

        //x축값 초기화
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Log.v("checkArray","xAxis.setValueFormatter:"+stringList.get((int)value));
                if(stringList.size()>(int) value){
                    return stringList.get((int)value);
                }else return null;
            }
        });

        YAxis yLAxis = lineChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);
        yLAxis.setLabelCount(11,true); //레이블 수
        yLAxis.setAxisMaxValue(300);
        yLAxis.setAxisMinValue(0);
        yLAxis.setSpaceTop(100);

        YAxis yRAxis = lineChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        Description description = new Description();
        description.setText("");

        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription(description);
        lineChart.animateY(2000, Easing.EasingOption.EaseInCubic);
        lineChart.invalidate();

        MyMarkerView marker = new MyMarkerView(this,R.layout.markerview_layout);
        marker.setChartView(lineChart);
        lineChart.setMarker(marker);
    }

    public static void setDayList(List graph_date, List graph_cnt) {
        Log.v("enterMethod","setDayList");
        ArrayList<String> array_date;
        ArrayList<String> array_cnt;

        array_date = (ArrayList<String>) graph_date;
        array_cnt = (ArrayList<String>) graph_cnt;

        int i=0;
        for(int p=0; p<array_date.size();p++){
            Log.v("checkArray","date["+p+"]:"+ array_date.get(p)+", cnt["+p+"]:"+ array_cnt.get(p));
            stringList.add(array_date.get(p));
            entries.add(new Entry(i, Float.parseFloat(array_cnt.get(p))));
            i++;
            Log.v("checkArray","stringList["+p+"]:"+ stringList.get(p)+", entries["+p+"]:"+ entries.get(p));
        }
        array_date.clear();
        array_cnt.clear();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("enteronBackPressed","onBackPressed");
        Intent intent = new Intent(this, LivePreviewActivity.class);
        startActivity(intent);
    }

    public void readFile() {
        Log.v("enterMethod","readFile");
        //전체 데이터 저장할 리스트
        ArrayList<String> read_date = new ArrayList<>();
        ArrayList<String> read_cnt = new ArrayList<>();
        int i=0,j=0;
        try {
            BufferedReader brd = new BufferedReader(new FileReader(getFilesDir() + "SquatDate.txt"));
            BufferedReader brc = new BufferedReader(new FileReader(getFilesDir() + "SquatCnt.txt"));
            String line;

            //전체 날짜 데이터 저장
            while((line = brd.readLine()) != null){
                read_date.add(i,line);
                i++;
            }
            brd.close();

            //전체 갯수 데이터 저장
            while((line = brc.readLine()) != null){
                read_cnt.add(j,line);
                j++;
            }
            brc.close();
            makeGraphData(read_date,read_cnt);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void makeGraphData(List dateList, List cntList){
        Log.v("enterMethod","makeGraphData");
        ArrayList<String> graph_date = new ArrayList<>();
        ArrayList<String> graph_cnt = new ArrayList<>();

        //그래프 표시할 배열에는 최근 7가지 데이터만 저장되도록
        if(dateList.size()>7){
            for(int i=0; i<dateList.size(); i++){
                if(i<dateList.size()-7){
                }else{
                    graph_date.add(graph_date.size(),(String) dateList.get(i));
                    graph_cnt.add(graph_cnt.size(),(String) cntList.get(i));
                }
            }
        }else{
            for(int t=0; t<dateList.size(); t++){
                graph_date.add(graph_date.size(), (String) dateList.get(t));
                graph_cnt.add(graph_cnt.size(), (String) cntList.get(t));
            }
        }
        //차트 액티비티로 표시할 데이터 전송
        setDayList(graph_date,graph_cnt);
    }
}