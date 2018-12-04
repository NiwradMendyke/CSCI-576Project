package finalproject;
import java.io.File; 
import java.io.IOException; 
import java.util.Scanner; 
  
import javax.sound.sampled.AudioInputStream; 
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.Clip; 
import javax.sound.sampled.LineUnavailableException; 
import javax.sound.sampled.UnsupportedAudioFileException; 

public class Audio {

    Long currentFrame;
    Clip clip;

    String status;

    AudioInputStream audioInputStream; 
    String filePath;

    public Audio(File file) throws UnsupportedAudioFileException, IOException, LineUnavailableException {

        audioInputStream = AudioSystem.getAudioInputStream(file); 

        clip = AudioSystem.getClip(null);

        clip.open(audioInputStream);

    }


    public void play() {

        clip.start();

        status = "play";
    }

    public void pause() {

        if (status.equals("paused"))  { 
            System.out.println("audio is already paused"); 
            return; 
        }

        this.currentFrame = this.clip.getMicrosecondPosition();
        clip.stop();
        status = "paused";

    }

    /*
    public void resumeAudio() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        clip.stop();
        clip.close();
        resetAudioStream();
        currentFrame = 0L;
        clip.setMicrosecondPosition(0);
        this.play();
    }
    */

    public void stop() {
        currentFrame = 0L; 
        clip.stop(); 
        clip.close(); 
    }

    public void jump(long c) {
         
        clip.setMicrosecondPosition(c); 
    }

    public void resetAudioStream(File file) throws UnsupportedAudioFileException, IOException, LineUnavailableException  
    { 
        audioInputStream = AudioSystem.getAudioInputStream(file);
        clip = AudioSystem.getClip(null);
        clip.open(audioInputStream); 
    }

}






                            

