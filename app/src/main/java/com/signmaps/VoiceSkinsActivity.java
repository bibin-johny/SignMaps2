package com.signmaps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.guidance.VoiceCatalog;
import com.here.android.mpa.guidance.VoiceSkin;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class VoiceSkinsActivity extends AppCompatActivity {
    private RecyclerView m_voiceSkinsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_skins);
        setTitle(R.string.voice_skins);

        m_voiceSkinsView = findViewById(R.id.voicesList);
        m_voiceSkinsView.setHasFixedSize(true);
        m_voiceSkinsView.setLayoutManager(new LinearLayoutManager(this));

        Button button = findViewById(R.id.downloadButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VoiceSkinsActivity.this, VoicePackagesActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshVoiceSkins();
    }

    private void refreshVoiceSkins() {
        // Fill the list of locally downloaded voices.
        VoiceCatalog catalog = VoiceCatalog.getInstance();
        List<VoiceSkin> voiceSkins = catalog.getLocalVoiceSkins();

        VoiceSkinsAdapter adapter = new VoiceSkinsAdapter(this, voiceSkins);
        m_voiceSkinsView.setAdapter(adapter);
    }

    private static class VoiceSkinsAdapter
            extends RecyclerView.Adapter<VoiceSkinsAdapter.VoiceViewHolder> {
        private List<VoiceSkin> m_voiceSkins;
        private LayoutInflater m_inflater;
        private long m_selectedId;

        VoiceSkinsAdapter(Context context, List<VoiceSkin> voiceSkins) {
            m_voiceSkins = voiceSkins;
            m_inflater = LayoutInflater.from(context);

            // get the id of the currently selected voice skin
            VoiceSkin selectedVoiceSkin =
                    NavigationManager.getInstance().getVoiceGuidanceOptions().getVoiceSkin();
            if (selectedVoiceSkin != null) {
                m_selectedId = selectedVoiceSkin.getId();
            } else {
                for (VoiceSkin voiceSkin : m_voiceSkins) {
                    if (voiceSkin.getLanguage().equals("None")) {
                        m_selectedId = voiceSkin.getId();
                        break;
                    }
                }
            }
        }

        @NonNull
        @Override
        public VoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = m_inflater.inflate(R.layout.voice_skin_item, parent, false);
            return new VoiceViewHolder(view);
        }

        @SuppressLint("SetTextI18n") @Override
        public void onBindViewHolder(@NonNull VoiceViewHolder holder, final int position) {
            VoiceSkin voiceSkin = m_voiceSkins.get(position);
            holder.m_idView.setText(Long.toString(voiceSkin.getId()));
            holder.m_languageView.setText(voiceSkin.getLanguage());
            holder.m_typeView.setText(voiceSkin.getOutputType().toString());
            holder.m_selectedView.setChecked(m_selectedId == voiceSkin.getId());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // set voice skin for navigation
                    NavigationManager.getInstance().getVoiceGuidanceOptions()
                            .setVoiceSkin(m_voiceSkins.get(position));
                    m_selectedId = m_voiceSkins.get(position).getId();
                    notifyItemRangeChanged(0, m_voiceSkins.size());
                }
            });
        }

        @Override
        public int getItemCount() {
            return m_voiceSkins.size();
        }

        private static class VoiceViewHolder extends RecyclerView.ViewHolder {
            RadioButton m_selectedView;
            TextView m_idView;
            TextView m_languageView;
            TextView m_typeView;

            VoiceViewHolder(View itemView) {
                super(itemView);

                m_selectedView = itemView.findViewById(R.id.voiceSelected);
                m_idView = itemView.findViewById(R.id.voiceId);
                m_languageView = itemView.findViewById(R.id.voiceLanguage);
                m_typeView = itemView.findViewById(R.id.voiceType);
            }
        }
    }
}