package com.signmaps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.io.IOException;

public class PopUp extends AppCompatDialogFragment {
    private EditText current;
    private EditText dest;
    private Listener elistener;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder =new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup,null);
        builder.setView(view)
                .setTitle("Enter Journey Details")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String starting = current.getText().toString();
                        String ending = dest.getText().toString();
                        try {
                            elistener.applyTexts(starting,ending);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                });
        current=view.findViewById(R.id.ed1);
        dest=view.findViewById(R.id.ed2);
        return builder.create();

    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            elistener=(Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+"must");

        }
    }

    public interface Listener{
        void applyTexts(String starting, String ending) throws IOException;
    }
}
