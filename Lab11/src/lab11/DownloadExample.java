package lab11;

import java.io.*;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadExample {

    static String[] toDownload = {
            "http://home.agh.edu.pl/pszwed/wyklad-c/01-jezyk-c-intro.pdf",
            "http://home.agh.edu.pl/~pszwed/wyklad-c/02-jezyk-c-podstawy-skladni.pdf",
            "http://home.agh.edu.pl/~pszwed/wyklad-c/03-jezyk-c-instrukcje.pdf",
            //"http://home.agh.edu.pl/~pszwed/wyklad-c/04-jezyk-c-funkcje.pdf",
            //"http://home.agh.edu.pl/~pszwed/wyklad-c/05-jezyk-c-deklaracje-typy.pdf",
            //"http://home.agh.edu.pl/~pszwed/wyklad-c/06-jezyk-c-wskazniki.pdf",
            //"http://home.agh.edu.pl/~pszwed/wyklad-c/07-jezyk-c-operatory.pdf",
            //"http://home.agh.edu.pl/~pszwed/wyklad-c/08-jezyk-c-lancuchy-znakow.pdf",
            //"http://home.agh.edu.pl/~pszwed/wyklad-c/09-jezyk-c-struktura-programow.pdf",
            //"http://home.agh.edu.pl/~pszwed/wyklad-c/10-jezyk-c-dynamiczna-alokacja-pamieci.pdf",
            //"http://home.agh.edu.pl/~pszwed/wyklad-c/11-jezyk-c-biblioteka-we-wy.pdf",
            //"http://home.agh.edu.pl/~pszwed/wyklad-c/preprocesor-make-funkcje-biblioteczne.pdf",
    };

    static AtomicInteger count = new AtomicInteger();

    static Semaphore sem = new Semaphore(0);

    DownloadExample() {
        for (String url : toDownload) {
            new Downloader(url).run();
        }
    }

    public static void main(String[] args) throws InterruptedException {
//        for (String url : toDownload) {
//            new Downloader(url).run();
//        }

        new Downloader().sequentialDownload();

        new Downloader().concurrentDownload();

        new Downloader().concurrentDownload2();

        new Downloader().concurrentDownload3();
    }

    static class Downloader extends Thread implements Runnable {
        private final String url;

        Downloader() {
            this.url = "";
        }

        Downloader(String url) {
            this.url = url;
        }

        public void run() {
            String fileName = this.url.substring(url.lastIndexOf('/')+1, url.length());

            try (InputStream in = new URL(url).openStream();
                 FileOutputStream out = new FileOutputStream(fileName)) {
                for (; ; ) {
                    // czytaj znak z in
                    // jeśli <0 break
                    //zapisz znak do out
                    int sign = in.read();
                    if (sign < 0) break;
                    out.write(sign);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Done:" + fileName);
            count.addAndGet(1);
            sem.release();
        }


        public static void sequentialDownload() {
            double t1 = System.nanoTime() / 1e6;
            for (String url : toDownload) {
                new Downloader(url).run();
            }
            double t2 = System.nanoTime() / 1e6;
            System.out.printf(Locale.US, "Sequential: t2-t1=%f count = %d\n", t2 - t1, count.get());
            count.set(0);
        }

        static void concurrentDownload() throws InterruptedException {
            double t1 = System.nanoTime()/1e6;

            for(String url : toDownload){

                // uruchom Downloader jako wątek...
                new Downloader(url).start();
            }

            double t2 = System.nanoTime()/1e6;

            sleep(1000);

            System.out.printf(Locale.US,"Current 1: t2-t1=%f count = %d\n",t2-t1, count.get());
            count.set(0);
        }

        static void concurrentDownload2() throws InterruptedException {
            double t1 = System.nanoTime() / 1e6;


            for (String url : toDownload) {

                // uruchom Downloader jako wątek...
                new Downloader(url).start();
            }

            while (!count.compareAndSet(toDownload.length, count.get())) {
                //sleep(1000);
                Thread.yield();
            }

            double t2 = System.nanoTime() / 1e6;

            System.out.printf(Locale.US, "Current 2: t2-t1=%f count = %d\n", t2 - t1, count.get());
            count.set(0);
        }


        static void concurrentDownload3() throws InterruptedException {
            double t1 = System.nanoTime() / 1e6;

            sem.acquire(sem.getQueueLength());

            for (String url : toDownload) {

                // uruchom Downloader jako wątek...
                new Downloader(url).start();
            }

            while (!count.compareAndSet(toDownload.length, count.get())) {
                //sleep(1000);
                Thread.yield();
            }

            double t2 = System.nanoTime() / 1e6;

            System.out.printf(Locale.US, "Current 3: t2-t1=%f count = %d\n", t2 - t1, count.get());
            count.set(0);
        }

    }
}
