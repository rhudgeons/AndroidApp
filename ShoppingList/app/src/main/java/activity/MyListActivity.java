package activity;

import android.annotation.TargetApi;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.example.rh035578.shoppinglist.R;
import com.wdullaer.swipeactionadapter.SwipeActionAdapter;
import com.wdullaer.swipeactionadapter.SwipeDirections;

import adapter.AlternateRowCursorAdapter;

/**
 * Created by rh035578 on 7/16/15.
 */
public class MyListActivity extends ListActivity implements SwipeActionAdapter.SwipeActionListener{

    protected SwipeActionAdapter swipeAdapter;
    private SQLiteDatabase dbRead;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Cursor cursor = readDB();

        String[] columns = {dbConstants.myConstants.FOOD, dbConstants.myConstants.PRICE, dbConstants.myConstants.QUANTITY};
        int[] to = {R.id.foodName, R.id.foodPrice, R.id.foodQuantity};
        AlternateRowCursorAdapter listAdapter = new AlternateRowCursorAdapter(this, R.layout.food_info, cursor, columns, to);

        swipeAdapter = new SwipeActionAdapter(listAdapter);
        swipeAdapter.setListView(getListView())
                    .setSwipeActionListener(this)
                    .setDimBackgrounds(true);

        setListAdapter(swipeAdapter);

        //just for testing purposes
        swipeAdapter.addBackground(SwipeDirections.DIRECTION_FAR_RIGHT,R.layout.row_bg)
                    .addBackground(SwipeDirections.DIRECTION_NORMAL_RIGHT, R.layout.row_bg);

        AddFoodFragment.getTotal(dbRead);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private Cursor readDB() {
        dbHelper helper = new dbHelper(this, dbConstants.myConstants.NAME, null, dbConstants.myConstants.VERSION);
        dbRead = helper.getReadableDatabase();

        String[] projection = {dbConstants.myConstants.ID,
                dbConstants.myConstants.FOOD,
                dbConstants.myConstants.PRICE,
                dbConstants.myConstants.QUANTITY};

        return dbRead.query(dbConstants.myConstants.GROCERY_LIST, projection, null, null, null, null, null);

    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id){
        //empty, do nothing on click
    }

    @Override
    public boolean hasActions(int position){
        return true;
    }

    @Override
    public boolean shouldDismiss(int position, int direction){
        return direction == SwipeDirections.DIRECTION_NORMAL_LEFT;
    }

    @Override
    public void onSwipe(int[] positionList, int[] directionList){
        for(int i=0;i<positionList.length;i++) {
            int position = positionList[i];

            Cursor deleteCursor = (Cursor) swipeAdapter.getItem(position);
            long deleteID;
            deleteID = deleteCursor.getLong(deleteCursor.getColumnIndex(dbConstants.myConstants.ID));
            Toast.makeText(this, "Food Deleted From List", Toast.LENGTH_SHORT).show();
            deleteFood(deleteID);

            swipeAdapter.notifyDataSetChanged();
        }
    }



    public void deleteFood(long id) {

        //delete food from grocery list
        dbHelper helper = new dbHelper(this, dbConstants.myConstants.NAME, null, dbConstants.myConstants.VERSION);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM " + dbConstants.myConstants.GROCERY_LIST + " WHERE " + dbConstants.myConstants.ID + " = " + id);

        //refresh activity after deletion
        restart();
    }

    public void restart() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        AddFoodFragment.getTotal(dbRead);
    }
}
