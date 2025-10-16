// src/components/MediaPlayers.tsx
import React from 'react';

interface YouTubePlayerProps {
  videoId: string;
}

export const YouTubePlayer: React.FC<YouTubePlayerProps> = ({ videoId }) => {
  return (
    <div className="aspect-video w-full mb-6 rounded-lg overflow-hidden shadow-lg border-4 border-[var(--dark-text)]">
      <iframe
        width="100%"
        height="100%"
        src={`https://www.youtube.com/embed/${videoId}`}
        frameBorder="0"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowFullScreen
        title="YouTube video player"
        className="w-full h-full"
      />
    </div>
  );
};

interface SpotifyPlayerProps {
  trackId: string;
}

export const SpotifyPlayer: React.FC<SpotifyPlayerProps> = ({ trackId }) => {
  return (
    <div className="w-full mb-6">
      <iframe
        style={{ borderRadius: '12px' }}
        src={`https://open.spotify.com/embed/track/${trackId}?utm_source=generator`}
        width="100%"
        height="152"
        frameBorder="0"
        allow="autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture"
        loading="lazy"
        title="Spotify player"
        className="shadow-lg"
      />
    </div>
  );
};

