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
        clip.loop(Clip.LOOP_CONTINUOUSLY); //Not sure if I need this

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

    public void resumeAudio() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        clip.stop();
        clip.close();
        resetAudioStream();
        currentFrame = 0L;
        clip.setMicrosecondPosition(0);
        this.play();
    }

    public void stop() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        currentFrame = 0L; 
        clip.stop(); 
        clip.close(); 
    }

    public void jump(long c) {
         
        clip.setMicrosecondPosition(c); 
    }

    public void resetAudioStream() throws UnsupportedAudioFileException, IOException, LineUnavailableException  
    { 
        audioInputStream = AudioSystem.getAudioInputStream( 
        new File(filePath).getAbsoluteFile()); 
        clip.open(audioInputStream); 
        clip.loop(Clip.LOOP_CONTINUOUSLY); 
    }

}






                            

