import os
import json
if __name__ == '__main__':
	for root, dirs, files in os.walk("../../data_generation/outputs/benchmark/"):
		for name in files:
			print(name)
                        if "json" not in name:
                                continue
			f = open(os.path.join(root, name))
			lines = f.readlines()
			outg = open("inp.f_g","w")
			outb = open("inp.f_b", "w")
			for l in lines:
				l = json.loads(l)
				outb.write(l['sentence_bad'].replace("n't"," not").replace(" .", ".").replace(" ?","?").replace("?"," ? ").replace("!"," ! ").replace("."," . "))
				outb.write("\n")
				outg.write(l['sentence_good'].replace("n't"," not").replace(" .", ".").replace(" ?","?").replace("?"," ? ").replace("!"," ! ").replace("."," . "))
				outg.write("\n")
                        outb.close();outg.close()
			correct = 0
			total = 0
                        resout = open("outs2/"+name,"w")
                        resout.write("good_sent good_lprob bad_sent bad_lprob")
                        resout.write("\n")
			os.system("java -ea -mx50000m -server -cp .:json-simple-1.1.1.jar:../src edu.berkeley.nlp.lm.io.ComputeLogProbabilityOfTextStream google_GPU.binary out.f_g inp.f_g")
			os.system("java -ea -mx50000m -server -cp .:json-simple-1.1.1.jar:../src edu.berkeley.nlp.lm.io.ComputeLogProbabilityOfTextStream  google_GPU.binary out.f_b inp.f_b")
			os.system("wc -l out.f_g");os.system("wc -l out.f_b")
                        with open('out.f_g') as f1, open('out.f_b') as f2, open('inp.f_g') as in1, open('inp.f_b') as in2:
				for g, b, ig, ib in zip(f1, f2, in1, in2):
					if float(g)>float(b):
						correct+=1
					total += 1
                                        resout.write(ig+" "+g+" "+ib+" "+b)
                                        resout.write("\n")
                        os.system("rm -rf out.f_b")
                        os.system("rm -rf out.f_g")
			print("For file " + name + "...")
			print("\n"+str(correct)+"/"+str(total)+"\n")
