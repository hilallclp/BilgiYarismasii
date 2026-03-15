package bilağları;

import java.io.*;
import java.net.*;
import java.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class JokerServer {
    private static final int JOKER_PORT = 4338;

    static class Question {
        String[] options;
        String correctAnswer;

        public Question(String[] options, String correctAnswer) {
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }

    private static List<Question> getQuestions() {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question(new String[]{"A", "B", "C", "D"}, "A"));
        questions.add(new Question(new String[]{"A", "B", "C", "D"}, "B"));
        questions.add(new Question(new String[]{"A", "B", "C", "D"}, "B"));
        questions.add(new Question(new String[]{"A", "B", "C", "D"}, "C"));
        questions.add(new Question(new String[]{"A", "B", "C", "D"}, "B"));
        return questions;
    }

    public static void main(String[] args) {
        List<Question> questions = getQuestions();

        try (ServerSocket serverSocket = new ServerSocket(JOKER_PORT)) {
            System.out.println("Joker Sunucusu başlatıldı. Bekleniyor...");

            while (true) {
                try (
                    Socket socket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
                ) {
                    System.out.println("İstek alındı.");
                    String request = in.readLine();
                    if (request == null || !request.contains(":")) continue;

                    String[] parts = request.split(":");
                    String jokerType = parts[0];
                    int questionIndex = Integer.parseInt(parts[1]);

                    if (questionIndex < 0 || questionIndex >= questions.size()) {
                        out.println("Geçersiz soru indeksi.");
                        out.println("END");
                        continue;
                    }

                    Question q = questions.get(questionIndex);

                    switch (jokerType) {
                        case "S":
                            Map<String, Integer> audience = generateAudiencePoll(q.correctAnswer, q.options);
                            for (String opt : q.options) {
                                out.println(opt + ") %" + audience.get(opt));
                            }
                            break;
                        case "Y":
                            List<String> reducedOptions = eliminateTwoWrongAnswers(q.correctAnswer, q.options);
                            for (String opt : reducedOptions) {
                                out.println(opt + ")");
                            }
                            break;
                        default:
                            out.println("Geçersiz joker tipi.");
                    }

                    out.println("END");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ✅ Doğru cevaba %40–60 arası ver, kalanları dağıt
    private static Map<String, Integer> generateAudiencePoll(String correct, String[] options) {
        Random rand = new Random();
        Map<String, Integer> poll = new LinkedHashMap<>();

        int correctPercentage = rand.nextInt(21) + 40; // 40–60
        poll.put(correct, correctPercentage);

        int remaining = 100 - correctPercentage;
        List<String> others = new ArrayList<>(Arrays.asList(options));
        others.remove(correct);

        for (int i = 0; i < others.size(); i++) {
            String opt = others.get(i);
            int val = (i == others.size() - 1) ? remaining : rand.nextInt(remaining + 1);
            poll.put(opt, val);
            remaining -= val;
        }

        return poll;
    }

    // ✅ Doğru cevap her zaman kalan şıklarda olsun
    private static List<String> eliminateTwoWrongAnswers(String correct, String[] options) {
        List<String> wrongs = new ArrayList<>();
        for (String opt : options) {
            if (!opt.equals(correct)) wrongs.add(opt);
        }

        Collections.shuffle(wrongs);
        wrongs = wrongs.subList(0, 2); // İki yanlış ele

        List<String> remaining = new ArrayList<>();
        remaining.add(correct); // Doğru şık eklendi

        for (String opt : options) {
            if (!wrongs.contains(opt) && !opt.equals(correct)) {
                remaining.add(opt); // Bir yanlış daha ekle (toplam 2 şık)
                break;
            }
        }

        Collections.shuffle(remaining); // Karıştır
        return remaining;
    }
}

