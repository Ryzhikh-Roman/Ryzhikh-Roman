import java.util.ArrayList;

// Объект, предоставляющий записанные данные
public class CustomLogger {
    private final ArrayList<String> messages;
    private int nextMessageIndex;
    private boolean endReached;

    public CustomLogger(){
        this.messages = new ArrayList<>();
        this.nextMessageIndex = 0;
        this.endReached = true;
    }

    // Добавить строку с сообщением
    public void addMessage(String message){
        if(this.isEndReached()){
            this.endReached = false;
        }
        this.messages.add(message);
    }

    // Получить следующее сообщение
    public String getNextMessage(){
        if(this.isEndReached()){
            return "";
        }
        StringBuilder ret = new StringBuilder();
        for (;nextMessageIndex < (this.messages.size() - 1); nextMessageIndex++){
            ret.append( this.messages.get(this.nextMessageIndex));
        }
        this.endReached = true;
        ret.append( this.messages.get(this.nextMessageIndex++));

        return ret.toString();
    }

    // Достигнут ли конец списка сообщений
    public boolean isEndReached(){
        return this.endReached;
    }
}
