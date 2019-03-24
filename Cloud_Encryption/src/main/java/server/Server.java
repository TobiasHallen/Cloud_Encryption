package server;
import express.Express;

public class Server 
{
    public static void main(String[] args) 
    {
        Express app = new Express();
        app.bind(new Bindings()); // See class below
        app.listen(8000);
    }

}
