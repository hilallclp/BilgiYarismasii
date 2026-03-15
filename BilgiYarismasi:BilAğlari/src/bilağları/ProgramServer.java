package bilağları;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;



import java.io.*;
import java.net.*;
import java.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class ProgramServer {
    private static final int SERVER_PORT = 4337;
    private static final String JOKER_HOST = "127.0.0.1";
    private static final int JOKER_PORT = 4338;

    static class Question {
        String questionText;
        String[] options;
        String correctAnswer;

        public Question(String questionText, String[] options, String correctAnswer) {
            this.questionText = questionText;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }

    private static List<Question> getQuestions() {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question("Python hangi yıl geliştirildi?", new String[]{"A) 1991", "B) 2000", "C) 1989", "D) 2010"}, "A"));
        questions.add(new Question("Java'nın geliştiricisi kimdir?", new String[]{"A) Bill Gates", "B) James Gosling", "C) Guido van Rossum", "D) Dennis Ritchie"}, "B"));
        questions.add(new Question("TCP/IP modelinde kaç katman vardır?", new String[]{"A) 3", "B) 4", "C) 5", "D) 7"}, "B"));
        questions.add(new Question("HTTP hangi port üzerinden çalışır?", new String[]{"A) 21", "B) 22", "C) 80", "D) 443"}, "C"));
        questions.add(new Question("Linux'un çekirdeği nedir?", new String[]{"A) Bash", "B) Kernel", "C) Ubuntu", "D) Shell"}, "B"));
        return questions;
    }

    private static final String[] prizeMessages = {
        "Linç Yükleniyor",
        "Önemli olan katılmaktı",
        "İki birden büyüktür",
        "Buralara kolay gelmedik",
        "Sen bu işi biliyorsun",
        "Harikasın"
    };

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Program Sunucusu başlatıldı. Bağlantı bekleniyor...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Yarışmacı bağlandı: " + clientSocket.getInetAddress());

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            List<Question> questions = getQuestions();
            int currentQuestion = 0;

            while (currentQuestion < questions.size()) {
                Question q = questions.get(currentQuestion);

                // Soru ve şıkları gönder
                out.println((currentQuestion + 1) + ". Soru: " + q.questionText);
                for (String opt : q.options) {
                    out.println(opt);
                }
                out.println("Jokerler: Seyirciye Sorma (S), Yarı Yarıya (Y)");
                out.println("Cevabınızı girin (A, B, C, D, S, Y):");

                String answer = in.readLine();
                if (answer == null) break;
                answer = answer.trim().toUpperCase();
                System.out.println("Cevap alındı: " + answer);

                // ⏰ TIMEOUT kontrolü
                if (answer.equals("TIMEOUT")) {
                    out.println("Yanıt verilmediği için yarışma sona erdi.");
                    System.out.println("Yarışma zaman aşımı nedeniyle sona erdi.");
                    break;
                }

                if (answer.equals("S") || answer.equals("Y")) {
                    System.out.println("Joker kullanıldı: " + (answer.equals("S") ? "Seyirciye Sorma" : "Yarı Yarıya"));

                    try (Socket jokerSocket = new Socket(JOKER_HOST, JOKER_PORT)) {
                        PrintWriter jokerOut = new PrintWriter(jokerSocket.getOutputStream(), true);
                        BufferedReader jokerIn = new BufferedReader(new InputStreamReader(jokerSocket.getInputStream()));

                        jokerOut.println(answer + ":" + currentQuestion);

                        String jokerResponse;
                        while((jokerResponse = jokerIn.readLine()) != null){
                            out.println(jokerResponse); // ← Güncellenen satır
                            if(jokerResponse.equals("END")) break; // ← Güncellenen satır
                        }
                    }catch (IOException e){
                        out.println("Joker sunucusuna bağlanılamadı.");
                        System.out.println("Joker sunucusuna bağlanılamadı.");
                    }
                } else {
                    if (answer.equals(q.correctAnswer)) {
                    	out.println("Doğru cevap!");
                    	out.println(prizeMessages[currentQuestion + 1]);

                        System.out.println("Doğru cevap! Soru " + (currentQuestion + 1) + " geçildi.");
                        currentQuestion++;
                    } else {
                        out.println("Yanlış cevap! Ödül: " + prizeMessages[currentQuestion]);
                        System.out.println("Yanlış cevap verildi. Doğru cevap: " + q.correctAnswer);
                        break;
                    }
                }
            }

            out.println("Yarışma sona erdi. Teşekkürler!");
            System.out.println("Yarışma sona erdi.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}







