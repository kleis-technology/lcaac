package ch.kleis.lcaplugin.imports.ecospold.lcai;

import java.io.InputStream;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import spold2.DataSet;
import spold2.ImpactMethod;

@XmlRootElement(name = "ecoSpold")
@XmlAccessorType(XmlAccessType.FIELD)
public class LcaJavaEcospold
{
    @XmlElement(name = "activityDataset")
    public DataSet dataSet = null;

    @XmlElement(name = "childActivityDataset")
    public DataSet childDataSet = null;

    @XmlElement(name = "impactMethod")
    public ImpactMethod impactMethod = null;


    static public LcaEcospold read(InputStream input)
    {
        try
        {
            return JAXB.unmarshal(input, LcaEcospold.class);
        }
        catch (Exception e)
        {
            var m = "failed to read EcoSpold 2 document";
            throw new RuntimeException(m, e);
        }
    }


}