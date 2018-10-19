package application;


import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;

public class BarcodeReader implements KeyListener, KeyEventDispatcher {

    private Timer timer = new Timer();
    private TimerTask resetTask;
    private Set<ActionListener> listeners = new HashSet<>();
    private static long BARCODE_READER_DELAY = 60;
    private static int MINIMAL_DIGITS = 8;
    private LinkedList<KeyEvent> queue = new LinkedList<>();
    private Set dispachSet = new HashSet<>();
    private long lastTime = 0;
    private List<KeyEvent> lastBarcodeEvents = new ArrayList<>();
    private Component parent;
    private boolean activeIfWindowInactive = false;
    private boolean activeIfParentInvisible = false;

    public BarcodeReader(Component parent) {
        this.parent = parent;
    }

    public BarcodeReader() {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(!check(e)){
            return;
        }
        processKeyEvent(e);
    }

    @Override
    public synchronized void keyTyped(KeyEvent e) {
        if(!check(e)){
            return;
        }
        processKeyEvent(e);
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        if(!check(e)){
            return;
        }
        processKeyEvent(e);
    }

    private class ResetTask extends TimerTask {

        @Override
        public void run() {
            emptyQueue(true);
        }
    };

    private void cancelTask() {
        if (resetTask != null) {
            resetTask.cancel();
        }
    }

    private void scheduleTask() {
        timer.schedule(resetTask = new ResetTask(), BARCODE_READER_DELAY);
    }

    private boolean check(KeyEvent e){
        if (dispachSet.contains(e)) {
            dispachSet.remove(e);
            return false;
        }
        return true;
    }
    
    private synchronized boolean processKeyEvent(KeyEvent e) {
        cancelTask();
        long time = System.currentTimeMillis();
        if (time - lastTime > BARCODE_READER_DELAY) {
            queue.addLast(e);
            emptyQueue(false);
            scheduleTask();
        } else {
            queue.addLast(e);
            if (e.getKeyChar() == '\n' && e.getID() == KeyEvent.KEY_RELEASED) {
                if (queue.size() < MINIMAL_DIGITS * 3) {
                    emptyQueue(false);
                    scheduleTask();
                } else {
                    fireActionEvent();
                }
            } else {
                scheduleTask();
            }
        }
        lastTime = time;
        return true;
    }

    private synchronized void emptyQueue(boolean full) {
        while (queue.size() > (full ? 0 : 1)) {
            KeyEvent e = queue.removeFirst();
            Object source = e.getSource();
            if (source instanceof Component) {
                Component comp = (Component) source;
                dispatchEvent(comp, e);
            }
        }
    }

    private void dispatchEvent(final Component source, final KeyEvent e) {
        dispachSet.add(e);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                source.dispatchEvent(e);
            }
        });
    }

    @Override
    public synchronized boolean dispatchKeyEvent(KeyEvent e) {
        if(!check(e)){
            return false;
        }
        if (parent != null) {
            if (!activeIfParentInvisible) {
                if(!parent.isShowing()){
                    return false;
                }
            }
            if (!activeIfWindowInactive) {
                Window w = SwingUtilities.getWindowAncestor(parent);
                if (w != null) {
                    if (!w.isActive()) {
                        return false;
                    }
                }
            }
        }
        long time = System.currentTimeMillis() - lastTime;
        if ((Character.isLetterOrDigit(e.getKeyChar())
                || e.getKeyChar() == '\n')) {
            return processKeyEvent(e);
        }
        return time < BARCODE_READER_DELAY;
    }

    private synchronized void fireActionEvent() {
        StringBuilder barcode = new StringBuilder();
        lastBarcodeEvents.clear();
        for (KeyEvent e : queue) {
            lastBarcodeEvents.add(e);
            if (e.getID() == KeyEvent.KEY_TYPED && e.getKeyChar() != '\n') {
                barcode.append(e.getKeyChar());
            }
        }
        queue.clear();
        if (barcode.length() > 0) {
            final ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, barcode.toString());
            for (final ActionListener l : listeners) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        l.actionPerformed(e);
                    }
                });

            }
        }
    }

    public synchronized void dispatchLastBarcodeAsKeyEvents() {
        for (KeyEvent e : lastBarcodeEvents) {
            dispatchEvent((Component) e.getSource(), e);
        }
    }

    public synchronized void addActionListener(ActionListener l) {
        listeners.add(l);
    }

    public synchronized void removeActionListener(ActionListener l) {
        listeners.remove(l);
    }

    public synchronized void removeAllActionListeners() {
        listeners.clear();
    }

    public void setParent(Component parent) {
        this.parent = parent;
    }

    public Component getParent() {
        return parent;
    }

    public boolean isActiveIfWindowInactive() {
        return activeIfWindowInactive;
    }

    public void setActiveIfWindowInactive(boolean activeIfWindowInactive) {
        this.activeIfWindowInactive = activeIfWindowInactive;
    }

    public boolean isActiveIfParentInvisible() {
        return activeIfParentInvisible;
    }

    public void setActiveIfParentInvisible(boolean activeIfParentInvisible) {
        this.activeIfParentInvisible = activeIfParentInvisible;
    }
    
    
}

